# MoodTail 경량 서버 부하 모니터링

## 범위

이 구성은 단일 `t3.micro` EC2의 서버 포화 상태만 관찰한다.

- EC2 호스트: CPU, Load Average, 메모리, Swap, 디스크, 네트워크
- MoodTail 애플리케이션 컨테이너: 실행 상태, CPU, 메모리, 재시작 횟수, OOM 상태
- Spring Boot JVM: Heap, GC, Thread, Process CPU
- 서버 내부 풀: HikariCP Connection Pool, Tomcat Thread Pool

다음 데이터는 수집하지 않는다.

- API 요청 수, URI, HTTP Method, 상태 코드, 오류율, 응답 시간
- 로그인 방식 및 로그인 횟수
- 테스트, 히스토리, 리포트 사용량
- 사용자 ID, 이메일, IP 등 개인정보 또는 사용자 식별값
- 애플리케이션 로그, Trace, Profile

`http.server.*`와 `http.client.*`는 애플리케이션의 `MeterFilter`에서 등록을 차단한다. Prometheus에서도 서버 부하 지표 이름을 허용 목록으로 한 번 더 제한한다.

## 구성

```text
EC2 t3.micro
├── moodtail-app (Spring Boot, 600M limit)
│   ├── :8080  Application API
│   └── :9091  Actuator (host loopback + Docker network only)
├── prometheus-agent (96M hard limit, 60s scrape)
├── node-exporter (32M hard limit, limited collectors)
└── systemd timer (1분마다 단발 실행)
    └── moodtail-app container metrics textfile
              │
              └── HTTPS remote_write ──> Grafana Cloud Free

CloudWatch Basic Monitoring
└── EC2 CPU, Network, Status Check, CPU Credit
```

Prometheus는 `--agent` 모드로 실행되므로 로컬 장기 TSDB, Query, Rule evaluation 기능을 사용하지 않는다. 원격 전송 장애에 대비한 WAL만 최대 1시간 유지한다. 각 수집 대상은 최대 500 sample과 20 label로 제한한다. Grafana와 cAdvisor는 EC2에 설치하지 않는다.

기존 `docker-compose.yml`과 모니터링 전용 `docker-compose.monitoring.yml`을 함께 사용한다. Redis 등 서버 구성이 기존 Compose에 추가되더라도 모니터링 서비스 정의와 직접 충돌하지 않도록 분리했다.

## 보안 경계

- 애플리케이션 API는 기존처럼 호스트 `8080`에 게시한다.
- Actuator `9091`은 호스트의 `127.0.0.1`에만 게시한다.
- Docker 내부에서는 Prometheus Agent만 `moodtail-app:9091`을 수집한다.
- 노출되는 Actuator endpoint는 `health`, `prometheus`뿐이다.
- Actuator discovery와 JMX endpoint 노출은 비활성화한다.
- Prometheus와 Node Exporter 포트는 호스트에 게시하지 않는다.
- Grafana Cloud Token은 GitHub Secrets와 EC2의 UID `65534` 전용 파일에만 둔다.

운영 보안 그룹에 `9090`, `9091`, `9100` 인바운드 규칙을 추가하지 않는다.

## GitHub Secrets

다음 Repository Secrets가 필요하다.

```text
GRAFANA_CLOUD_PROMETHEUS_URL
GRAFANA_CLOUD_PROMETHEUS_USERNAME
GRAFANA_CLOUD_PROMETHEUS_TOKEN
```

Token은 현재 Stack에 대한 `metrics:write` 권한만 가져야 한다. 저장소, 이슈, PR, 채팅, 일반 `.env` 파일에 값을 기록하지 않는다.

배포 워크플로는 URL과 Username을 Prometheus 설정 템플릿에 넣고 Token은 별도 파일로 생성한다. Token은 애플리케이션의 배포 `.env`에 포함하지 않는다.

## 배포 및 검증

배포 워크플로는 다음 순서로 동작한다.

1. Gradle 전체 빌드와 테스트
2. Grafana Cloud Secret 형식 검증
3. Prometheus runtime 설정과 Token 파일 생성
4. EC2에 Compose 및 모니터링 파일 복사
5. Prometheus 실행 UID `65534`의 설정 및 Token 읽기 권한 확인
6. 공식 `promtool`로 Prometheus 설정 검증
7. 세 컨테이너 시작
8. 호스트 loopback의 `http://localhost:9091/actuator/health`와 애플리케이션 `8080` 응답 확인
9. `http://localhost:9091/actuator/prometheus`에서 대표 JVM 지표 반환 확인
10. 컨테이너 세 개의 실행 상태 확인
11. Container metrics systemd timer 활성화

자동 검증과 배포 후 sample 확인이 끝나기 전에는 운영 배포가 완료된 것으로 판단하지 않는다.

배포 후 2~3분이 지나면 Prometheus Agent 로그에 반복되는 scrape 또는 `remote_write` 오류가 없는지 확인한다. Grafana Cloud Explore에서는 `up{project="moodtail"}`의 최근 sample이 들어오는지 확인한다.

## Grafana 대시보드 가져오기

1. Grafana Cloud에서 **Dashboards → New → Import**로 이동한다.
2. `monitoring/grafana/moodtail-server-load-dashboard.json`을 업로드한다.
3. `DS_PROMETHEUS`에 Grafana Cloud 기본 Prometheus data source를 선택한다.
4. 대시보드를 저장한다.

배포 전에는 정상적으로 `No data`가 표시된다. 배포 후 최대 2~3분 동안 첫 수집과 전송을 기다린다.

## 권장 Grafana Alert 규칙

Grafana Cloud Alerting에서 다음 PromQL을 사용한다. Prometheus Agent는 로컬 Rule을 평가하지 않는다.

### 가용 메모리 부족

```promql
node_memory_MemAvailable_bytes{job="moodtail-node"} < 100 * 1024 * 1024
```

- Pending: 5분
- 우선순위: Critical
- 즉시 모니터링 스택 중단을 검토한다.

### 지속적인 Swap 입출력

```promql
rate(node_vmstat_pswpin{job="moodtail-node"}[5m])
+ rate(node_vmstat_pswpout{job="moodtail-node"}[5m]) > 0
```

- Pending: 10분
- 우선순위: Warning

### 호스트 CPU 과부하

```promql
100 * (1 - avg(rate(node_cpu_seconds_total{job="moodtail-node",mode="idle"}[5m]))) > 80
```

- Pending: 10분
- 우선순위: Warning

### 루트 디스크 부족

```promql
node_filesystem_avail_bytes{job="moodtail-node",mountpoint="/"}
/ node_filesystem_size_bytes{job="moodtail-node",mountpoint="/"} < 0.15
```

- Pending: 10분
- 우선순위: Warning

### 애플리케이션 컨테이너 중단

```promql
moodtail_container_running{job="moodtail-node",service="moodtail-app"} == 0
```

- Pending: 2분
- 우선순위: Critical

### 애플리케이션 컨테이너 메모리 부족

```promql
moodtail_container_memory_usage_ratio{
  job="moodtail-node",
  service="moodtail-app"
} > 0.85
```

- Pending: 5분
- 우선순위: Warning

### JVM Heap 부족

```promql
sum(jvm_memory_used_bytes{job="moodtail-spring",area="heap"})
/ clamp_min(sum(jvm_memory_max_bytes{job="moodtail-spring",area="heap"}), 1) > 0.85
```

- Pending: 10분
- 우선순위: Warning

### DB Connection 대기

```promql
hikaricp_connections_pending{job="moodtail-spring"} > 0
```

- Pending: 5분
- 우선순위: Warning

### Prometheus Agent 수집 중단

```promql
absent_over_time(up{project="moodtail"}[5m]) == 1
```

- Pending: 1분
- 우선순위: Critical
- No Data 처리: `Alerting`

`No Data = Alerting`은 Prometheus Agent 수집 중단을 감지하는 heartbeat 알림에만 적용한다. 메모리, Swap, CPU, 디스크, JVM, DB Connection 등 리소스 알림은 모니터링을 의도적으로 중단하거나 배포하기 전에도 불필요한 경고가 발생하지 않도록 No Data 처리를 기본값 또는 `Normal`로 둔다.

## CloudWatch 무료 범위 사용

EC2 기본 모니터링만 사용한다.

- `CPUUtilization`
- `NetworkIn`, `NetworkOut`
- `StatusCheckFailed`, `StatusCheckFailed_Instance`, `StatusCheckFailed_System`
- `CPUCreditUsage`, `CPUCreditBalance`
- Unlimited 모드인 경우 `CPUSurplusCreditBalance`, `CPUSurplusCreditsCharged`

CloudWatch Agent, Logs, Detailed Monitoring, Custom Metrics는 활성화하지 않는다. EC2 기본 모니터링의 일반 지표와 CPU Credit 지표는 5분 주기로 확인한다.

T3는 기본적으로 Unlimited 모드일 수 있다. `CPUSurplusCreditsCharged`가 증가하면 추가 비용이 발생할 수 있으므로 CPU 사용률뿐 아니라 CPU Credit도 함께 확인한다.

## Redis 컨테이너 추가 시 주의사항

Redis 구성은 이 모니터링 변경에 포함하지 않는다. 서버 담당자가 Redis 서비스를 추가할 때 다음을 다시 확인한다.

- `redis`를 Compose 내부에만 노출하고 호스트 `6379` 포트는 게시하지 않는다.
- 애플리케이션의 `SPRING_DATA_REDIS_HOST`는 Compose service name을 사용한다.
- Redis `maxmemory`와 eviction 정책은 저장 데이터 특성에 맞게 별도로 결정한다.
- Redis 추가 직후 24시간 동안 `MemAvailable`, Swap, OOM, 애플리케이션 지연을 다시 관찰한다.
- Redis Exporter는 현재 메모리 예산에서 추가하지 않는다.

현재 측정치는 대략 다음과 같다.

```text
EC2 RAM total       908MiB
EC2 RAM available   204MiB
Swap used           419MiB / 1GiB
Application memory  약 399MiB / 600MiB
```

Prometheus Agent와 Node Exporter의 hard limit 합계는 128MiB다. Limit은 예약량이 아니지만 실제 사용량이 증가할 수 있다. Redis까지 같은 호스트에 추가하면 여유 메모리가 100MiB 아래로 떨어질 가능성이 있으므로 동시에 무관찰 상태로 적용하지 않는다.

## 24시간 Pilot 점검

배포 직후와 1시간, 6시간, 24시간 후에 다음을 확인한다.

```bash
cd ~/app
free -h
vmstat 1 10
sudo docker stats --no-stream
sudo docker compose --env-file .env \
  -f docker-compose.yml \
  -f docker-compose.monitoring.yml \
  ps
sudo docker inspect --format \
  'RestartCount={{.RestartCount}} OOMKilled={{.State.OOMKilled}}' \
  MoodTail-server
sudo systemctl status moodtail-container-metrics.timer --no-pager
sudo journalctl -u moodtail-container-metrics.service -n 50 --no-pager
```

다음 중 하나라도 발생하면 모니터링 스택부터 중단한다.

- `MemAvailable`이 100MiB 미만으로 5분 이상 지속
- `si`, `so`가 지속적으로 0보다 큼
- MoodTail 애플리케이션 재시작 또는 OOM
- Prometheus Agent 반복 OOM
- API 체감 지연 증가
- `CPUSurplusCreditsCharged` 증가

## 모니터링만 중단하는 방법

애플리케이션과 Redis 구성은 유지하고 모니터링만 중단한다.

```bash
cd ~/app
sudo docker compose --env-file .env \
  -f docker-compose.yml \
  -f docker-compose.monitoring.yml \
  stop prometheus-agent node-exporter
sudo systemctl disable --now moodtail-container-metrics.timer
```

다시 시작할 때는 다음을 실행한다.

```bash
cd ~/app
sudo docker compose --env-file .env \
  -f docker-compose.yml \
  -f docker-compose.monitoring.yml \
  up -d node-exporter prometheus-agent
sudo systemctl enable --now moodtail-container-metrics.timer
sudo systemctl start moodtail-container-metrics.service
```

## 문제 확인

```bash
cd ~/app
sudo docker compose --env-file .env \
  -f docker-compose.yml \
  -f docker-compose.monitoring.yml \
  logs --tail=200 prometheus-agent node-exporter
curl --fail --silent http://localhost:9091/actuator/health
curl --silent --output /dev/null --write-out '%{http_code}\n' http://localhost:8080/
curl --fail --silent http://localhost:9091/actuator/prometheus \
  | grep -m 1 '^jvm_memory_used_bytes'
sudo -u '#65534' -- test -r monitoring/prometheus/runtime/prometheus.yml
sudo -u '#65534' -- test -r monitoring/prometheus/runtime/grafana-cloud-token
```

Prometheus Agent 로그에 인증 실패, scrape 실패, `remote_write` 재시도 메시지가 반복되지 않아야 한다. Grafana Cloud Explore에서 `up{project="moodtail"}`을 조회해 최근 sample 시각도 함께 확인한다.

Token 만료 또는 폐기 후에는 GitHub Secret `GRAFANA_CLOUD_PROMETHEUS_TOKEN`을 교체하고 재배포한다.

## 참고 자료

- [Spring Boot Actuator endpoint 보안](https://docs.spring.io/spring-boot/3.4/reference/actuator/endpoints.html)
- [Prometheus Agent Mode](https://prometheus.io/docs/prometheus/latest/prometheus_agent/)
- [Prometheus command flags](https://prometheus.io/docs/prometheus/latest/command-line/prometheus/)
- [Node Exporter](https://github.com/prometheus/node_exporter)
- [Grafana Cloud Prometheus remote_write](https://grafana.com/docs/grafana-cloud/send-data/metrics/metrics-prometheus/)
- [EC2 Basic/Detailed Monitoring](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/manage-detailed-monitoring.html)
- [EC2 CPU Credit 모니터링](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/burstable-performance-instances-monitoring-cpu-credits.html)
