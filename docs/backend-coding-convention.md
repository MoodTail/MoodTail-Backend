# Backend Coding Convention

이 문서는 Moodtail 백엔드 코드 작성 규칙을 정리한다.

Moodtail은 감정 기반 테스트 결과를 5가지 맛 지표로 수치화하고, 96종 IBA 칵테일 데이터와 유사도 분석을 수행하여 사용자에게 가장 잘 맞는 칵테일을 추천하는 서비스다. 코드 구조와 네이밍은 이 도메인 흐름이 드러나도록 작성한다.

## 1. Package Structure

기본 패키지 구조는 `global`과 `domain`으로 나눈다.

```text
global/
domain/
```

`global`에는 프로젝트 전역에서 사용하는 공통 코드를 둔다.

```text
global/
  config/
  exception/
  response/
  security/
  util/
```

`domain`에는 도메인별 기능 코드를 둔다.

```text
domain/
  auth/
  member/
  moodtest/
  recommendation/
  cocktail/
  history/
  collection/
  report/
  share/
```

도메인 내부 구조는 아래 형식을 기본으로 한다.

```text
domain/
  cocktail/
    controller/
      docs/
    service/
    repository/
    entity/
    enums/
    dto/
      request/
      response/
```

계층별 역할은 다음과 같다.

```text
Controller: 요청과 응답 처리
ControllerDocs: Swagger 문서 작성
Service: 비즈니스 로직 처리
Repository: 데이터 접근 처리
Entity: 도메인 데이터 표현
Enum: 도메인 Enum 타입 표현
DTO: API 요청/응답 데이터 전달
```

Controller에는 비즈니스 로직을 작성하지 않는다.

Service에서 트랜잭션을 관리한다.

Entity는 API 응답으로 직접 반환하지 않는다.

도메인에서 사용하는 Enum은 `entity` 패키지에 두지 않고 도메인별 `enums` 패키지에 둔다.

```text
domain/
  cocktail/
    entity/
    enums/
      BaseSpirit
      AlcoholRange

  moodtest/
    entity/
    enums/
      MoodQuestionType
      TasteMetric

  recommendation/
    entity/
    enums/
      MoodType
      RecommendationStatus
```

## 2. Moodtail Domain Rule

Moodtail의 주요 도메인은 다음 기준으로 나눈다.

```text
auth            소셜 로그인, 토큰 발급, 로그아웃
member          회원, 마이페이지, 회원 탈퇴
moodtest        감정 테스트 문항, 답변, 테스트 결과
recommendation  맛 지표 계산, 타입 매칭, 칵테일 추천
cocktail        칵테일 DB, 레시피, 재료, 즐겨찾기
history         일별 테스트 기록, 마신 칵테일 기록
collection      12타입 캐릭터 도감, 타입 잠금 해제
report          월간 리포트
share           결과 공유 카드, 공유 메타데이터
```

추천 로직의 책임은 `recommendation` 도메인에 둔다. `moodtest`는 사용자의 답변과 테스트 결과 생성 흐름을 담당하고, `cocktail`은 칵테일 원천 데이터와 상세 정보를 담당한다.

## 3. Naming

클래스명은 역할이 드러나도록 작성한다.

```text
AuthController
MemberService
MoodTestController
RecommendationService
CocktailRepository
HistoryController
MonthlyReportService
```

기본 네이밍 규칙은 다음을 따른다.

```text
Class: PascalCase
method: camelCase
variable: camelCase
constant: UPPER_SNAKE_CASE
package: lowercase
```

DTO는 요청과 응답을 분리한다.

```text
XxxRequest
XxxResponse
XxxCreateRequest
XxxUpdateRequest
XxxSubmitRequest
XxxDetailResponse
XxxSummaryResponse
```

Moodtail 도메인 DTO 예시는 다음과 같다.

```text
OAuthLoginRequest
TokenResponse
MoodTestQuestionResponse
MoodTestSubmitRequest
MoodTestResultResponse
TasteProfileResponse
RecommendationResultResponse
RecommendedCocktailResponse
CocktailDetailResponse
CocktailIngredientResponse
CocktailFavoriteResponse
DailyHistoryCreateRequest
DailyHistoryResponse
MonthlyReportResponse
ShareCardResponse
```

## 4. API Rule

API 경로는 다음 형식을 따른다.

```text
/api/v1/{resources}
```

리소스명은 복수형 명사를 사용한다.

```text
GET    /api/v1/cocktails
GET    /api/v1/cocktails/{cocktailId}
POST   /api/v1/cocktails/{cocktailId}/favorites
DELETE /api/v1/cocktails/{cocktailId}/favorites

GET    /api/v1/mood-tests/questions
POST   /api/v1/mood-test-results
GET    /api/v1/mood-test-results/{resultId}

GET    /api/v1/histories?year=2026&month=6
POST   /api/v1/histories
PATCH  /api/v1/histories/{historyId}
DELETE /api/v1/histories/{historyId}

GET    /api/v1/reports/monthly?year=2026&month=6
POST   /api/v1/share-cards
```

동작은 URL이 아니라 HTTP Method로 표현한다.

```text
Bad:  /api/v1/cocktails/search
Good: GET /api/v1/cocktails?keyword=martini

Bad:  /api/v1/histories/delete/{historyId}
Good: DELETE /api/v1/histories/{historyId}
```

HTTP Method는 다음 기준으로 사용한다.

```text
GET: 조회
POST: 생성
PATCH: 일부 수정
DELETE: 삭제
```

인증처럼 외부 OAuth 흐름 때문에 동작 표현이 필요한 경우에도 API 목적이 명확하게 드러나도록 작성한다.

```text
POST /api/v1/auth/login/{provider}
POST /api/v1/auth/logout
POST /api/v1/auth/reissue
```

## 5. Common Response

모든 API 응답은 공통 응답 형식을 사용한다.

성공 응답:

```json
{
  "success": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "data": {}
}
```

실패 응답:

```json
{
  "success": false,
  "code": "COMMON_400",
  "message": "잘못된 요청입니다.",
  "data": null
}
```

응답 데이터가 없는 경우 `data`는 `null`로 반환한다.

목록 응답은 `items`를 사용한다.

```json
{
  "success": true,
  "code": "COMMON_200",
  "message": "요청에 성공했습니다.",
  "data": {
    "items": []
  }
}
```

페이지네이션이 필요한 경우 `pageInfo`를 함께 반환한다.

```json
{
  "items": [],
  "pageInfo": {
    "page": 0,
    "size": 20,
    "totalElements": 96,
    "totalPages": 5,
    "hasNext": true
  }
}
```

## 6. Taste Metric Rule

Moodtail의 맛 지표는 5가지를 사용한다.

```text
alcoholIntensity  도수
sweetness         당도
sourness          산도
refreshing        청량감
bitterness        쓴맛
```

맛 지표 값은 기본적으로 1~5점 척도를 사용한다.

```text
minimum: 1
maximum: 5
```

소수점 계산이 필요한 추천 로직에서는 `double` 또는 `BigDecimal` 중 프로젝트에서 정한 하나를 일관되게 사용한다. API 응답에서는 프론트엔드 레이더 차트가 바로 사용할 수 있도록 동일한 필드명과 동일한 순서를 유지한다.

레이더 차트 응답 순서는 다음 순서로 고정한다.

```text
alcoholIntensity -> sweetness -> sourness -> refreshing -> bitterness
```

맛 지표를 직접 숫자 배열로만 반환하지 않는다. 각 값의 의미를 알 수 있도록 필드명 또는 label을 함께 제공한다.

```json
{
  "alcoholIntensity": 3.5,
  "sweetness": 4.0,
  "sourness": 2.5,
  "refreshing": 4.5,
  "bitterness": 1.5
}
```

## 7. Recommendation Rule

감정 테스트는 고정 문항 5개와 랜덤 문항 2개를 기준으로 한다.

```text
fixed questions: 70%
random questions: 30%
```

모든 답변은 1~5점 척도로 변환하여 추천 로직으로 전달한다.

추천 로직은 유클리드 거리 기반 유사도 분석을 기본으로 한다.

```text
distance = sqrt(sum((userMetric - targetMetric)^2))
```

거리값이 낮을수록 사용자와 더 유사한 대상으로 판단한다.

```text
sort: distance ASC
```

사용자 타입은 12가지 타입 중 유사도가 가장 높은 타입 1개를 할당한다.

추천 칵테일은 사용자의 맛 지표와 칵테일 맛 지표 간 거리를 기준으로 정렬한다. 결과 페이지 기본 노출 개수는 상위 4종으로 확정한다.

```text
recommendedCocktailLimit = 4
```

추천 개수, 가중치, 타입 수, 맛 지표 수가 변경되면 관련 상수, 테스트 코드, Swagger 예시를 함께 수정한다.

Controller에는 추천 계산식을 작성하지 않는다. 추천 계산은 `RecommendationService` 또는 추천 도메인 내부 계산 컴포넌트에서 처리한다.

## 8. Exception

예외는 `GlobalExceptionHandler`에서 공통 처리한다.

비즈니스 예외는 커스텀 예외를 사용한다.

```text
BusinessException
ErrorCode
```

에러 코드는 다음 형식을 따른다.

```text
{DOMAIN}_{HTTP_STATUS}
```

Moodtail 에러 코드 예시는 다음과 같다.

```text
COMMON_400
AUTH_401
MEMBER_404
MOOD_TEST_400
RECOMMENDATION_422
COCKTAIL_404
HISTORY_409
REPORT_409
SHARE_500
```

Controller에서 `try-catch`로 예외를 직접 처리하지 않는다.

검증 실패, 권한 실패, 비즈니스 정책 위반은 각각 명확한 ErrorCode로 구분한다.

```text
MOOD_TEST_400       테스트 답변 누락 또는 잘못된 답변 값
RECOMMENDATION_422  추천 결과 산출 불가
COCKTAIL_404        칵테일을 찾을 수 없음
HISTORY_409         같은 날짜에 이미 저장된 테스트 결과가 있음
REPORT_409          월간 리포트 생성에 필요한 데이터 부족
```

## 9. DTO / Validation

Request DTO와 Response DTO는 분리한다.

Entity를 Request DTO 또는 Response DTO로 직접 사용하지 않는다.

Request DTO에는 필요한 validation annotation을 사용한다.

```java
@NotBlank
@NotNull
@Size
@Email
@Min
@Max
```

Controller에서는 `@Valid`를 사용한다.

```java
@PostMapping
public ResponseEntity<ApiResponse<MoodTestResultResponse>> submitMoodTest(
        @Valid @RequestBody MoodTestSubmitRequest request
) {
    ...
}
```

단순 입력값 검증은 DTO에서 처리하고, 비즈니스 규칙 검증은 Service에서 처리한다.

```text
DTO validation: 답변 개수, 필수 값, 숫자 범위
Service validation: 일일 저장 제한, 월간 리포트 최소 기록 수, 추천 가능 여부
```

## 10. Entity Rule

Entity에는 `@Setter`를 사용하지 않는다.

Entity의 기본 생성자는 `protected`로 제한한다.

```java
@NoArgsConstructor(access = AccessLevel.PROTECTED)
```

값 변경은 의미 있는 메서드로 처리한다.

```java
public void updateSatisfaction(int satisfaction) {
    this.satisfaction = satisfaction;
}
```

생성 로직이 복잡한 경우 정적 팩토리 메서드를 사용할 수 있다.

```java
public static MoodTestResult create(Member member, TasteProfile tasteProfile, MoodType moodType) {
    return MoodTestResult.builder()
            .member(member)
            .tasteProfile(tasteProfile)
            .moodType(moodType)
            .build();
}
```

## 11. Transaction

트랜잭션은 Service 계층에서 관리한다.

조회 메서드는 `readOnly = true`를 사용한다.

```java
@Transactional(readOnly = true)
```

생성, 수정, 삭제 메서드는 일반 `@Transactional`을 사용한다.

```java
@Transactional
```

Controller에는 `@Transactional`을 사용하지 않는다.

추천 계산처럼 DB 상태 변경이 없는 로직은 가능하면 읽기 전용 트랜잭션 또는 트랜잭션 없는 순수 계산 컴포넌트로 분리한다.

## 12. Lombok

Lombok은 필요한 annotation만 제한적으로 사용한다.

주로 사용하는 annotation:

```text
@Getter
@Builder
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
```

사용하지 않는 annotation:

```text
@Data
@Setter
```

`@AllArgsConstructor`는 Entity에 무분별하게 사용하지 않는다. 필요한 경우 접근 제어자를 명시한다.

## 13. Security / Auth

소셜 로그인 provider는 enum으로 관리한다.

```text
GOOGLE
KAKAO
```

인증이 필요한 API와 게스트 접근이 가능한 API를 명확히 구분한다.

```text
public: 온보딩, 로그인, 칵테일 샘플 조회
authenticated: 히스토리, 즐겨찾기, 도감, 마이페이지, 월간 리포트
policy-dependent: 테스트 수행, 결과 저장, 공유 카드 생성
```

게스트 정책이 변경될 수 있는 기능은 Controller에 임의로 조건문을 흩뿌리지 않는다. Security 설정, 권한 annotation, 정책 컴포넌트 중 하나로 일관되게 관리한다.

## 14. Configuration

민감 정보는 Git에 커밋하지 않는다.

환경별 설정 파일은 다음 기준을 따른다.

```text
application.yaml       공통 설정
application-local.yaml 로컬 MySQL 설정
application-dev.yaml   개발 서버 설정
application-prod.yaml  운영 MySQL 설정
application-test.yaml  테스트 H2 설정
```

민감 정보는 환경 변수 또는 별도 로컬 설정 파일로 관리한다.

`.env` 파일은 커밋하지 않고, 필요한 환경 변수 예시는 `.env.example`에 작성한다.

문서 파일은 `docs/` 하위에 관리한다. 단, 민감 정보가 포함된 로컬 문서, 외부 서비스 키, 운영 계정 정보는 커밋하지 않는다.
