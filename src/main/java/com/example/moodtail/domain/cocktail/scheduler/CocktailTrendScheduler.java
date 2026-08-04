package com.example.moodtail.domain.cocktail.scheduler;

import com.example.moodtail.domain.cocktail.service.CocktailTrendSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 칵테일 트렌드 스냅샷을 주기적으로 갱신하는 스케줄러
@Slf4j
@Component
@RequiredArgsConstructor
public class CocktailTrendScheduler {

    // 갱신 주기 10분 - cron 표현식으로 관리해서 나중에 쉽게 변경 가능하게 함
    private static final String TREND_REFRESH_CRON = "0 0/10 * * * *";

    private final CocktailTrendSnapshotService cocktailTrendSnapshotService;

    @Scheduled(cron = TREND_REFRESH_CRON)
    public void refreshTrendSnapshot() {
        cocktailTrendSnapshotService.refreshSnapshot();
    }

    // 서버 기동 직후 스냅샷 테이블이 비어있는 상태로 트렌드 API가 응답하지 않도록 1회 선실행
    @EventListener(ApplicationReadyEvent.class)
    public void refreshOnStartup() {
        try {
            cocktailTrendSnapshotService.refreshSnapshot();
        } catch (RuntimeException e) {
            log.error("기동 시 칵테일 트렌드 스냅샷 갱신에 실패했습니다. 다음 스케줄에서 재시도합니다.", e);
        }
    }
}
