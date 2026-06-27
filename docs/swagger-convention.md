# Swagger Convention

이 문서는 Moodtail 백엔드의 Swagger 문서 작성 규칙을 정리한다.

Swagger 문서는 프론트엔드와 백엔드가 같은 API 계약을 기준으로 개발하기 위한 문서다. Moodtail의 핵심 흐름인 소셜 로그인, 감정 테스트, 맛 지표, 추천 결과, 칵테일 상세, 히스토리, 월간 리포트, 공유 카드가 API 문서에서 명확히 드러나야 한다.

## 1. 기본 원칙

- Swagger 문서는 API 명세를 설명하기 위한 용도로만 작성한다.
- Swagger 문서 코드는 실제 Controller와 분리한다.
- 실제 요청 처리 annotation은 Controller에 작성한다.
- Swagger 설명 annotation은 `XxxControllerDocs` 인터페이스에 작성한다.
- DTO 필드 설명은 Request/Response DTO에 작성한다.
- Service, Repository, Entity에는 Swagger annotation을 작성하지 않는다.
- API가 변경되면 Controller, ControllerDocs, DTO Schema, 테스트를 함께 확인한다.

## 2. 패키지 구조

Swagger 문서 인터페이스는 각 도메인의 `controller/docs` 패키지에 둔다.

```text
domain/
  recommendation/
    controller/
      RecommendationController
      docs/
        RecommendationControllerDocs
```

인터페이스 이름은 `XxxControllerDocs` 형식을 따른다.

Controller는 해당 `XxxControllerDocs`를 구현한다.

```java
@RestController
@RequiredArgsConstructor
public class RecommendationController implements RecommendationControllerDocs {
    ...
}
```

## 3. Tag 작성 규칙

API 그룹 설명은 `@Tag`로 작성한다.

Moodtail의 기본 Tag는 다음을 사용한다.

```text
Auth            소셜 로그인, 토큰 재발급, 로그아웃 API
Members         회원, 마이페이지 API
Mood Tests      감정 테스트 문항, 제출, 결과 API
Recommendations 추천 결과, 맛 지표 매칭 API
Cocktails       칵테일 목록, 상세, 레시피, 즐겨찾기 API
Histories       일별 테스트 기록, 음주 기록 API
Collections     12타입 캐릭터 도감 API
Reports         월간 리포트 API
Shares          결과 공유 카드 API
```

Tag 이름은 API 목록에서 읽기 쉽도록 영어 복수형을 사용하고, 설명은 한국어로 작성한다.

## 4. Controller 작성 규칙

Controller에는 실제 API 동작과 관련된 Spring MVC annotation만 작성한다.

Controller에는 Swagger annotation을 작성하지 않는다.

```text
Do not use in Controller:
- @Operation
- @ApiResponse
- @ApiResponses
- @Tag
- @Schema
```

Controller에 작성하는 annotation 예시는 다음과 같다.

```java
@RestController
@RequestMapping("/api/v1/cocktails")
@RequiredArgsConstructor
```

## 5. ControllerDocs 작성 규칙

ControllerDocs에는 Swagger 문서화와 관련된 내용만 작성한다.

API 설명은 `@Operation`으로 작성한다.

응답 설명은 필요한 경우 `@ApiResponses`와 `@ApiResponse`로 작성한다.

`operationId`는 Controller 메서드명과 동일하게 작성한다.

`summary`는 짧고 명확하게 작성한다.

`description`은 필요한 경우에만 작성하고, `summary`와 같은 내용을 반복하지 않는다.

```java
@Operation(
        operationId = "submitMoodTest",
        summary = "감정 테스트 제출",
        description = "7개 문항 답변을 5가지 맛 지표로 변환하고 사용자에게 가장 가까운 타입과 추천 칵테일을 반환합니다."
)
```

summary는 사용자가 API 목록에서 바로 이해할 수 있는 문장으로 작성한다.

```text
Good:
- 감정 테스트 문항 조회
- 감정 테스트 제출
- 추천 결과 조회
- 칵테일 상세 조회
- 월간 리포트 조회

Bad:
- 조회
- 테스트
- 기능 처리
```

## 6. DTO Schema 작성 규칙

Request DTO와 Response DTO에는 필요한 경우 `@Schema`를 작성한다.

`@Schema`의 `description`과 `example`은 API 사용자가 이해할 수 있는 값으로 작성한다.

Entity에는 `@Schema`를 작성하지 않는다.

```java
@Schema(description = "도수 지표. 1에 가까울수록 낮고 5에 가까울수록 높습니다.", example = "3.5")
private double alcoholIntensity;
```

맛 지표 필드는 아래 이름과 설명을 사용한다.

```text
alcoholIntensity  도수 지표
sweetness         당도 지표
sourness          산도 지표
refreshing        청량감 지표
bitterness        쓴맛 지표
```

맛 지표 값의 범위는 1~5점으로 명시한다.

```java
@Schema(description = "당도 지표. 1~5점 척도입니다.", example = "4.0", minimum = "1", maximum = "5")
private double sweetness;
```

레이더 차트용 응답은 프론트엔드가 그대로 사용할 수 있도록 필드명과 순서를 일관되게 유지한다.

```json
{
  "alcoholIntensity": 3.5,
  "sweetness": 4.0,
  "sourness": 2.5,
  "refreshing": 4.5,
  "bitterness": 1.5
}
```

## 7. 응답 작성 규칙

응답 문서는 코딩 컨벤션의 공통 응답 형식을 기준으로 작성한다.

성공 응답은 `ApiResponse<T>` 형태로 문서화한다.

```json
{
  "success": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "data": {}
}
```

실패 응답은 공통 에러 응답 형식을 따른다.

```json
{
  "success": false,
  "code": "MOOD_TEST_400",
  "message": "테스트 답변이 올바르지 않습니다.",
  "data": null
}
```

공통 에러 응답은 중복 작성을 피하고, 필요한 경우 공통 설정에서 관리한다.

도메인별 특수 에러만 ControllerDocs에 명시한다.

```text
AUTH_401             인증이 필요함
MEMBER_404           회원을 찾을 수 없음
MOOD_TEST_400        테스트 답변 누락 또는 잘못된 답변 값
RECOMMENDATION_422   추천 결과 산출 불가
COCKTAIL_404         칵테일을 찾을 수 없음
HISTORY_409          일일 저장 정책 위반
REPORT_409           리포트 생성 데이터 부족
```

## 8. Moodtail API 예시 기준

감정 테스트 제출 API는 추천 근거가 드러나도록 문서화한다.

결과 페이지 추천 칵테일 기본 노출 개수는 4종으로 확정한다. Swagger 예시와 설명도 이 기준을 따른다.

```json
{
  "resultId": 1,
  "matchedType": {
    "typeCode": "FRESH_SPARK",
    "typeName": "상큼한 스파클러",
    "description": "가볍고 청량한 한 잔이 잘 어울리는 타입입니다."
  },
  "tasteProfile": {
    "alcoholIntensity": 2.0,
    "sweetness": 3.5,
    "sourness": 4.0,
    "refreshing": 4.5,
    "bitterness": 1.0
  },
  "recommendedCocktails": [
    {
      "cocktailId": 10,
      "name": "Mojito",
      "matchRate": 92,
      "tasteKeywords": ["청량한", "상큼한", "가벼운"]
    }
  ]
}
```

칵테일 상세 API는 사용자가 맛과 제조 정보를 바로 이해할 수 있도록 문서화한다.

```json
{
  "cocktailId": 10,
  "name": "Mojito",
  "baseSpirit": "RUM",
  "alcoholDegree": 15.0,
  "tasteProfile": {
    "alcoholIntensity": 2.0,
    "sweetness": 3.0,
    "sourness": 4.0,
    "refreshing": 5.0,
    "bitterness": 1.0
  },
  "ingredients": [
    {
      "name": "White Rum",
      "amountMl": 45
    }
  ],
  "pairingSnacks": ["라임칩", "과일 플래터"]
}
```

월간 리포트 API는 데이터 부족 조건을 명시한다.

```text
월간 기록이 5건 미만이면 REPORT_409를 반환한다.
```

## 9. 인증 문서 작성 규칙

인증이 필요한 API는 Swagger 문서에 SecurityRequirement를 명시한다.

```java
@Operation(
        operationId = "getHistories",
        summary = "월별 히스토리 조회",
        security = @SecurityRequirement(name = "bearerAuth")
)
```

게스트 접근이 가능한 API와 로그인 사용자 전용 API를 설명에 명확히 적는다.

```text
비로그인 사용자는 즐겨찾기, 히스토리, 도감, 마이페이지 API를 사용할 수 없습니다.
```

## 10. 문서 작성 기준

예시는 실제 운영 정보나 민감 정보를 사용하지 않는다.

예시 값은 API 사용자가 바로 이해할 수 있는 값으로 작성한다.

외부 OAuth access token, refresh token, 실제 이메일, 실제 사용자 이름은 예시에 사용하지 않는다.

API가 변경되면 Controller와 ControllerDocs를 함께 수정한다.

맛 지표, 추천 개수, 타입 수, 에러 코드가 변경되면 Swagger 예시도 함께 수정한다.
