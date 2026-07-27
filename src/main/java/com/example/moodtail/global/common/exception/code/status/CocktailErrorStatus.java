package com.example.moodtail.global.common.exception.code.status;

import com.example.moodtail.global.common.exception.code.BaseCodeDto;
import com.example.moodtail.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor

public enum CocktailErrorStatus implements BaseCodeInterface {
    INVALID_ALCOHOL_DEGREE_RANGE(HttpStatus.BAD_REQUEST, "COCKTAIL400", "칵테일 도수 범위가 올바르지 않습니다."),
    COCKTAIL_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "MOOD_TYPE404", "해당 칵테일 타입을 찾을 수 없습니다."),
    COCKTAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "COCKTAIL_404", "해당 칵테일을 찾을 수 없습니다."),
    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND, "RECIPE_404", "해당 레시피를 찾을 수 없습니다."),
    INGREDIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "INGREDIENT_404", "해당 재료를 찾을 수 없습니다."),
    COCKTAIL_FAVORITE_ALREADY_EXISTS(HttpStatus.CONFLICT, "COCKTAIL409", "이미 즐겨찾기에 추가된 칵테일입니다."),
    COCKTAIL_FAVORITE_NOT_FOUND(HttpStatus.NOT_FOUND, "FAVORITE_404", "즐겨찾기에 추가되지 않은 칵테일입니다.");

    private final HttpStatus httpStatus;
    private final boolean isSuccess = false;
    private final String code;
    private final String message;

    @Override
    public BaseCodeDto getCode() {
        return BaseCodeDto.builder()
                .httpStatus(httpStatus)
                .isSuccess(isSuccess)
                .code(code)
                .message(message)
                .build();
    }
}
