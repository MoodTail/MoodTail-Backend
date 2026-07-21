package com.example.moodtail.domain.moodtest.controller.docs;

import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultRequest;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestQuestionResponse;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Mood Tests", description = "무드 테스트 문항 조회 및 결과 산출 API")
public interface MoodTestControllerDocs {

    String QUESTIONS_SUCCESS_EXAMPLE = """
            {
              "timestamp":"2026-07-21T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.",
              "result":{"totalCount":7,"questions":[
                {"questionId":1,"questionType":"FIXED","content":"오늘 원하는 술의 강도는?","sortOrder":1,"options":[{"optionId":1,"content":"가볍게","optionOrder":1},{"optionId":2,"content":"진하게","optionOrder":2}]},
                {"questionId":2,"questionType":"FIXED","content":"어떤 단맛을 원하나요?","sortOrder":2,"options":[{"optionId":3,"content":"달지 않게","optionOrder":1},{"optionId":4,"content":"달콤하게","optionOrder":2}]},
                {"questionId":3,"questionType":"FIXED","content":"상큼한 맛은 어느 정도가 좋나요?","sortOrder":3,"options":[{"optionId":5,"content":"은은하게","optionOrder":1},{"optionId":6,"content":"상큼하게","optionOrder":2}]},
                {"questionId":4,"questionType":"FIXED","content":"청량감은 어느 정도가 좋나요?","sortOrder":4,"options":[{"optionId":7,"content":"부드럽게","optionOrder":1},{"optionId":8,"content":"톡 쏘게","optionOrder":2}]},
                {"questionId":5,"questionType":"FIXED","content":"쓴맛은 어느 정도가 좋나요?","sortOrder":5,"options":[{"optionId":9,"content":"적게","optionOrder":1},{"optionId":10,"content":"쌉쌀하게","optionOrder":2}]},
                {"questionId":11,"questionType":"RANDOM","content":"지금 떠오르는 분위기는?","sortOrder":1,"options":[{"optionId":21,"content":"고요한 밤","optionOrder":1},{"optionId":22,"content":"활기찬 파티","optionOrder":2}]},
                {"questionId":14,"questionType":"RANDOM","content":"오늘의 음악을 고른다면?","sortOrder":4,"options":[{"optionId":27,"content":"잔잔한 재즈","optionOrder":1},{"optionId":28,"content":"경쾌한 팝","optionOrder":2}]}
              ]}
            }
            """;
    String RESULT_SUCCESS_EXAMPLE = """
            {
              "timestamp":"2026-07-21T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.",
              "result":{"resultId":null,"saved":false,
                "moodType":{"moodTypeId":2001,"typeCode":"TYPE01","name":"몽글몽글 낭만파","shortDescription":"부드러운 달콤함 속에서 여유를 즐기는 타입","characterQuote":"오늘은 천천히, 달콤하게 즐겨볼래요.","characterImageUrl":"https://cdn.moodtail.com/mood-types/type01.png","displayTasteScores":{"alcoholIntensity":25,"sweetness":75,"sourness":50,"refreshing":50,"bitterness":25}},
                "tasteProfile":{"alcoholIntensity":2.0,"sweetness":4.0,"sourness":3.0,"refreshing":3.0,"bitterness":2.0},
                "displayTasteScores":{"alcoholIntensity":25,"sweetness":75,"sourness":50,"refreshing":50,"bitterness":25},
                "recommendations":[
                  {"ranking":1,"cocktailId":101,"nameKo":"피나 콜라다","nameEn":"Pina Colada","shortDescription":"달콤하고 부드러운 트로피컬 칵테일","imageUrl":"https://cdn.moodtail.com/cocktails/101.png","matchScore":96},
                  {"ranking":2,"cocktailId":102,"nameKo":"준벅","nameEn":"June Bug","shortDescription":"멜론 향이 돋보이는 상큼한 칵테일","imageUrl":"https://cdn.moodtail.com/cocktails/102.png","matchScore":92},
                  {"ranking":3,"cocktailId":103,"nameKo":"블루 하와이","nameEn":"Blue Hawaii","shortDescription":"청량한 열대 과일 풍미의 칵테일","imageUrl":"https://cdn.moodtail.com/cocktails/103.png","matchScore":88},
                  {"ranking":4,"cocktailId":104,"nameKo":"코스모폴리탄","nameEn":"Cosmopolitan","shortDescription":"새콤달콤하고 세련된 칵테일","imageUrl":"https://cdn.moodtail.com/cocktails/104.png","matchScore":84}],
                "compatibilities":{
                  "best":{"moodTypeId":2002,"typeCode":"TYPE02","name":"반짝이는 모험가","shortDescription":"새로운 자극을 즐기는 타입","characterQuote":"새로운 한 잔을 만나러 가볼까요?","characterImageUrl":"https://cdn.moodtail.com/mood-types/type02.png","displayTasteScores":{"alcoholIntensity":50,"sweetness":50,"sourness":75,"refreshing":75,"bitterness":25}},
                  "worst":{"moodTypeId":2006,"typeCode":"TYPE06","name":"묵직한 사색가","shortDescription":"깊고 쌉쌀한 풍미를 즐기는 타입","characterQuote":"한 잔에 담긴 이야기를 음미해요.","characterImageUrl":"https://cdn.moodtail.com/mood-types/type06.png","displayTasteScores":{"alcoholIntensity":75,"sweetness":25,"sourness":25,"refreshing":25,"bitterness":100}}}
              }
            }
            """;
    String MOOD_TEST400_QUERY_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"MOOD_TEST400","message":"테스트 문항 조회에 실패했습니다."}
            """;
    String COMMON402_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON402","message":"입력값 검증에 실패했습니다."}
            """;
    String MOOD_TEST400_ANSWER_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"MOOD_TEST400","message":"답변 개수 또는 형식이 올바르지 않습니다."}
            """;
    String MOOD_TEST404_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"MOOD_TEST404","message":"문항 또는 선택지를 찾을 수 없습니다."}
            """;
    String RECOMMENDATION422_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"RECOMMENDATION422","message":"추천 결과를 산출할 수 없습니다."}
            """;
    String COMMON500_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON500","message":"서버 에러가 발생했습니다."}
            """;

    @Operation(operationId = "getMoodTestQuestions", summary = "테스트 질문 조회",
            description = "활성 고정 문항 5개와 랜덤 문항 2개를 합쳐 총 7개 문항을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 테스트 질문 조회 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = QUESTIONS_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "MOOD_TEST400 - 문항 구성 또는 조회 결과가 올바르지 않음",
                    content = @Content(examples = @ExampleObject(name = "MOOD_TEST400", value = MOOD_TEST400_QUERY_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<MoodTestQuestionResponse> getQuestions();

    @Operation(operationId = "calculateMoodTestResult", summary = "테스트 제출 및 결과 산출",
            description = "7개 문항의 답변으로 맛 프로필, 무드 타입, 추천 칵테일 4종과 타입 궁합을 산출합니다. "
                    + "결과를 저장하지 않으므로 resultId는 null이고 saved는 false입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 테스트 결과 산출 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = RESULT_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "COMMON402 - 요청값 검증 실패\nMOOD_TEST400 - 답변 개수 또는 형식 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "MOOD_TEST400", value = MOOD_TEST400_ANSWER_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "404", description = "MOOD_TEST404 - 문항 또는 선택지를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(name = "MOOD_TEST404", value = MOOD_TEST404_EXAMPLE))),
            @ApiResponse(responseCode = "422", description = "RECOMMENDATION422 - 타입 또는 추천 결과 산출 불가",
                    content = @Content(examples = @ExampleObject(name = "RECOMMENDATION422", value = RECOMMENDATION422_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<MoodTestResultResponse> calculateResult(MoodTestResultRequest request);
}
