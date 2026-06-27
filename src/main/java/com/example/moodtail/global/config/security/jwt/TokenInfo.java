package com.example.moodtail.global.config.security.jwt;

import lombok.Builder;

@Builder
public record TokenInfo(
	String accessToken,
	String refreshToken
) {

}
//자바 레코드란?
//자바 14부터 릴리즈된 기능으로, 불변의 데이터를 쉽게 표현하기 위한 기능
//레코드는 클래스와 비슷하지만, 데이터를 저장하기 위한 목적으로만 사용
//레코드는 final로 선언되어 있어서 불변
//레코드는 생성자, 게터, equals, hashCode, toString 메소드를 자동으로 생성해줌
