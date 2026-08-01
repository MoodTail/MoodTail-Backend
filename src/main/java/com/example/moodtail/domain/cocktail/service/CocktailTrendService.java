package com.example.moodtail.domain.cocktail.service;

import com.example.moodtail.domain.cocktail.dto.response.CocktailTrendResponse;
import com.example.moodtail.domain.cocktail.entity.CocktailTrendSnapshot;
import com.example.moodtail.domain.cocktail.repository.CocktailTrendSnapshotRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CocktailTrendService {

    private final CocktailTrendSnapshotRepository cocktailTrendSnapshotRepository;
    private final ObjectMapper objectMapper;

    // 스케줄러가 미리 계산해 둔 최신 스냅샷을 조회만 한다 (계산 로직은 CocktailTrendSnapshotService 참고)
    @Transactional(readOnly = true)
    public CocktailTrendResponse getTrend() {
        CocktailTrendSnapshot snapshot = cocktailTrendSnapshotRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new IllegalStateException("칵테일 트렌드 스냅샷이 아직 생성되지 않았습니다."));
        return deserialize(snapshot.getSnapshotData());
    }

    private CocktailTrendResponse deserialize(String snapshotData) {
        try {
            return objectMapper.readValue(snapshotData, CocktailTrendResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("칵테일 트렌드 스냅샷 역직렬화에 실패했습니다.", e);
        }
    }
}
