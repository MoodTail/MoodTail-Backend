package com.example.moodtail.domain.cocktail.dto.response;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import lombok.Builder;

import java.util.List;
import java.util.Set;

@Builder
public record MoodTypeResponse(
        Long moodTypeId,
        String typeCode,
        String name,
        String shortDescription,
        String description,
        String catchphrase,
        String characterImageUrl,
        boolean unlocked,
        boolean representative,
        boolean canSetRepresentative,
        int typePercent,
        int collectionRate,
        TypeFiguresDto typeFigures,
        CompatibilitiesDto compatibilities,
        List<CocktailSummaryDto> cocktails,
        int totalCocktailCount,
        int unlockedCocktailCount
) {
    public static MoodTypeResponse from(
            MoodType moodType,
            boolean unlocked,
            boolean representative,
            boolean canSetRepresentative,
            int typePercent,
            int collectionRate,
            TypeFiguresDto typeFigures,
            MoodType bestMatch,
            MoodType worstMatch,
            List<Cocktail> cocktails,
            Set<Long> unlockedCocktailIds
    ){
        List<CocktailSummaryDto> cocktailResponses =
                cocktails.stream()
                        .map(cocktail ->
                                CocktailSummaryDto.from(
                                        cocktail,
                                        unlockedCocktailIds.contains(
                                                cocktail.getId()
                                        )
                                )
                        )
                        .toList();
        return MoodTypeResponse.builder()
                .moodTypeId(moodType.getId())
                .typeCode(moodType.getCode())
                .name(moodType.getName())
                .shortDescription(
                        moodType.getShortDescription()
                )
                .description(moodType.getDescription())
                .catchphrase(moodType.getCharacterQuote())
                .characterImageUrl(
                        getImageUrl(
                                moodType.getCharacterImage()
                        )
                )
                .unlocked(unlocked)
                .representative(representative)
                .canSetRepresentative(canSetRepresentative)
                .typePercent(typePercent)
                .collectionRate(collectionRate)
                .typeFigures(typeFigures)
                .compatibilities(
                        CompatibilitiesDto.of(
                                bestMatch,
                                worstMatch
                        )
                )
                .cocktails(cocktailResponses)
                .totalCocktailCount(cocktails.size())
                .unlockedCocktailCount(
                        unlockedCocktailIds.size()
                )
                .build();
    }
    @Builder
    public record TypeFiguresDto(
            int alcoholIntensity,
            int sweetness,
            int sourness,
            int bitterness,
            int refreshing
    ) {
        public static TypeFiguresDto of(
                int alcoholIntensity,
                int sweetness,
                int sourness,
                int bitterness,
                int refreshing
        ) {
            return new TypeFiguresDto(
                    alcoholIntensity,
                    sweetness,
                    sourness,
                    bitterness,
                    refreshing
            );
        }
    }

    @Builder
    public record CompatibilitiesDto(
            CompatibilityDto best,
            CompatibilityDto worst
    ) {
        public static CompatibilitiesDto of(
                MoodType best,
                MoodType worst
        ) {
            return new CompatibilitiesDto(
                    CompatibilityDto.from(best),
                    CompatibilityDto.from(worst)
            );
        }
    }

    @Builder
    public record CompatibilityDto(
            Long moodTypeId,
            String typeCode,
            String name,
            String characterImageUrl
    ) {
        public static CompatibilityDto from(
                MoodType moodType
        ) {
            if (moodType == null) {
                return null;
            }

            return new CompatibilityDto(
                    moodType.getId(),
                    moodType.getCode(),
                    moodType.getName(),
                    getImageUrl(
                            moodType.getCharacterImage()
                    )
            );
        }
    }

    @Builder
    public record CocktailSummaryDto(
            Long cocktailId,
            String nameKo,
            String nameEn,
            String shortDescription,
            String imageUrl,
            boolean unlocked
    ) {
        public static CocktailSummaryDto from(
                Cocktail cocktail,
                boolean unlocked
        ) {
            return new CocktailSummaryDto(
                    cocktail.getId(),
                    cocktail.getNameKo(),
                    cocktail.getNameEn(),
                    cocktail.getShortDescription(),
                    getImageUrl(cocktail.getImage()),
                    unlocked
            );
        }
    }

    private static String getImageUrl(
            com.example.moodtail.domain.image.entity.Image image
    ) {
        return image == null
                ? null
                : image.getImageUrl();
    }
}
