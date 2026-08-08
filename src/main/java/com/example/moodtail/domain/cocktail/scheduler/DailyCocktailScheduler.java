package com.example.moodtail.domain.cocktail.scheduler;

import com.example.moodtail.domain.cocktail.entity.Region;
import com.example.moodtail.domain.cocktail.service.DailyCocktailGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyCocktailScheduler {

    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final DailyCocktailGenerationService generationService;

    @Scheduled(
            cron = "0 0 0 * * *",
            zone = "Asia/Seoul"
    )
    public void generateAtMidnight() {
        generateAllRegions();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void generateOnStartup() {
        try {
            generateAllRegions();
        } catch (RuntimeException exception) {
            log.error(
                    "서버 기동 시 오늘의 칵테일 생성 실패",
                    exception
            );
        }
    }

    private void generateAllRegions() {
        LocalDate today =
                LocalDate.now(SEOUL_ZONE_ID);

        for (Region region : Region.values()) {
            try {
                generationService.generate(
                        today,
                        region
                );

                log.info(
                        "오늘의 칵테일 생성 완료: date={}, region={}",
                        today,
                        region
                );
            } catch (RuntimeException exception) {
                log.error(
                        "오늘의 칵테일 생성 실패: date={}, region={}",
                        today,
                        region,
                        exception
                );
            }
        }
    }
}
