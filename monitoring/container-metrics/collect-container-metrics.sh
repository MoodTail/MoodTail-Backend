#!/bin/sh

set -eu

OUTPUT_DIRECTORY=${1:-/var/lib/moodtail-monitoring/textfile}
OUTPUT_FILE="${OUTPUT_DIRECTORY}/moodtail-containers.prom"
MONITORED_SERVICES="moodtail-app"

mkdir -p "$OUTPUT_DIRECTORY"
TEMP_FILE=$(mktemp "${OUTPUT_DIRECTORY}/.moodtail-containers.XXXXXX")
trap 'rm -f "$TEMP_FILE"' EXIT INT TERM

human_to_bytes() {
	raw_value=$1
	case "$raw_value" in
		*KiB) number=${raw_value%KiB}; multiplier=1024 ;;
		*MiB) number=${raw_value%MiB}; multiplier=1048576 ;;
		*GiB) number=${raw_value%GiB}; multiplier=1073741824 ;;
		*kB) number=${raw_value%kB}; multiplier=1000 ;;
		*MB) number=${raw_value%MB}; multiplier=1000000 ;;
		*GB) number=${raw_value%GB}; multiplier=1000000000 ;;
		*B) number=${raw_value%B}; multiplier=1 ;;
		*) number=0; multiplier=1 ;;
	esac
	awk -v number="$number" -v multiplier="$multiplier" \
		'BEGIN { if (number ~ /^[0-9]+([.][0-9]+)?$/) printf "%.0f", number * multiplier; else print 0 }'
}

write_metric_headers() {
	cat <<'EOF'
# HELP moodtail_container_running Whether the monitored Docker Compose service is running.
# TYPE moodtail_container_running gauge
# HELP moodtail_container_cpu_usage_percent Current container CPU usage percentage.
# TYPE moodtail_container_cpu_usage_percent gauge
# HELP moodtail_container_memory_usage_bytes Current container memory usage in bytes.
# TYPE moodtail_container_memory_usage_bytes gauge
# HELP moodtail_container_memory_limit_bytes Configured container memory limit in bytes.
# TYPE moodtail_container_memory_limit_bytes gauge
# HELP moodtail_container_memory_usage_ratio Current container memory usage divided by its limit.
# TYPE moodtail_container_memory_usage_ratio gauge
# HELP moodtail_container_restarts_total Total Docker restarts for the current container.
# TYPE moodtail_container_restarts_total counter
# HELP moodtail_container_oom_killed Whether the container's current state reports an OOM kill.
# TYPE moodtail_container_oom_killed gauge
# HELP moodtail_container_metrics_collector_success Whether the most recent collection completed successfully.
# TYPE moodtail_container_metrics_collector_success gauge
# HELP moodtail_container_metrics_collector_last_success_unixtime Unix time of the most recent successful collection.
# TYPE moodtail_container_metrics_collector_last_success_unixtime gauge
EOF
}

write_service_metrics() {
	service=$1
	container_ids=
	if ! container_ids=$(docker ps -aq \
		--filter "label=com.docker.compose.service=${service}" 2>/dev/null); then
		COLLECTOR_SUCCESS=0
	fi
	container_id=$(printf '%s\n' "$container_ids" | sed -n '1p')

	running=0
	cpu_percent=0
	memory_usage_bytes=0
	memory_limit_bytes=0
	memory_usage_ratio=0
	restart_count=0
	oom_killed=0

	if [ -n "$container_id" ]; then
		inspect_values=$(docker inspect \
			--format '{{.State.Running}}|{{.State.OOMKilled}}|{{.RestartCount}}|{{.HostConfig.Memory}}' \
			"$container_id" 2>/dev/null || true)
		if [ -n "$inspect_values" ]; then
			running_value=$(printf '%s' "$inspect_values" | cut -d '|' -f 1)
			oom_value=$(printf '%s' "$inspect_values" | cut -d '|' -f 2)
			restart_count=$(printf '%s' "$inspect_values" | cut -d '|' -f 3)
			memory_limit_bytes=$(printf '%s' "$inspect_values" | cut -d '|' -f 4)

			[ "$running_value" = "true" ] && running=1
			[ "$oom_value" = "true" ] && oom_killed=1
		else
			COLLECTOR_SUCCESS=0
		fi

		if [ "$running" -eq 1 ]; then
			stats=$(docker stats --no-stream --format '{{.CPUPerc}}|{{.MemUsage}}' \
				"$container_id" 2>/dev/null || true)
			if [ -n "$stats" ]; then
				cpu_raw=$(printf '%s' "$stats" | cut -d '|' -f 1 | tr -d '% ')
				memory_raw=$(printf '%s' "$stats" | cut -d '|' -f 2 | cut -d '/' -f 1 | tr -d ' ')
				cpu_percent=$(awk -v value="$cpu_raw" \
					'BEGIN { if (value ~ /^[0-9]+([.][0-9]+)?$/) print value + 0; else print 0 }')
				memory_usage_bytes=$(human_to_bytes "$memory_raw")
			else
				COLLECTOR_SUCCESS=0
			fi
		fi
	fi

	if [ "$memory_limit_bytes" -gt 0 ] 2>/dev/null; then
		memory_usage_ratio=$(awk -v usage="$memory_usage_bytes" -v limit="$memory_limit_bytes" \
			'BEGIN { printf "%.6f", usage / limit }')
	fi

	cat <<EOF
moodtail_container_running{service="${service}"} ${running}
moodtail_container_cpu_usage_percent{service="${service}"} ${cpu_percent}
moodtail_container_memory_usage_bytes{service="${service}"} ${memory_usage_bytes}
moodtail_container_memory_limit_bytes{service="${service}"} ${memory_limit_bytes}
moodtail_container_memory_usage_ratio{service="${service}"} ${memory_usage_ratio}
moodtail_container_restarts_total{service="${service}"} ${restart_count}
moodtail_container_oom_killed{service="${service}"} ${oom_killed}
EOF
}

COLLECTOR_SUCCESS=1
write_metric_headers > "$TEMP_FILE"

if ! docker info >/dev/null 2>&1; then
	COLLECTOR_SUCCESS=0
fi

for service_name in $MONITORED_SERVICES; do
	write_service_metrics "$service_name" >> "$TEMP_FILE"
done

printf 'moodtail_container_metrics_collector_success %s\n' "$COLLECTOR_SUCCESS" >> "$TEMP_FILE"
if [ "$COLLECTOR_SUCCESS" -eq 1 ]; then
	printf 'moodtail_container_metrics_collector_last_success_unixtime %s\n' "$(date +%s)" >> "$TEMP_FILE"
else
	printf 'moodtail_container_metrics_collector_last_success_unixtime 0\n' >> "$TEMP_FILE"
fi

chmod 0644 "$TEMP_FILE"
mv "$TEMP_FILE" "$OUTPUT_FILE"
trap - EXIT INT TERM
