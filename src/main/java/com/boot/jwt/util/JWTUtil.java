package com.boot.jwt.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//@Component와 @Configuration은 Spring Framework에서 빈(Bean)을 등록하기 위한 주요 어노테이션
//@Component 어노테이션이 붙은 클래스는 해당 클래스의 객체가 스프링 애플리케이션 컨텍스트에 빈으로 등록되어 관리
//@Configuration 어노테이션이 붙은 클래스 내부에서 @Bean 어노테이션을 사용하여 빈을 등록 + 스프링의 자바 기반 설정 클래스를 정의하는 데 사용
@Component
@Log4j2
public class JWTUtil {

    @Value("${com.boot.jwt.secret}")
    private String key;

    //JWT 문자열을 생성   (헤더 / 페이로드 / 서명 알고리즘)
    public String generateToken(Map<String, Object> valueMap, int days){

        log.info("generateKey..." + key);

        //헤더 부분
        Map<String, Object> headers = new HashMap<>();
        headers.put("typ", "JWT");      //토큰 유형 (JWT)
        headers.put("alg", "HS256");    //서명 알고리즘

        //payload 부분
        Map<String, Object> payloads = new HashMap<>();
        payloads.putAll(valueMap);

        //테스트 시에는 짧은 유효기간 (JWT 문자열 생성에서 plusMinutes로 설정한걸 확인 가능)
        int time = (60*24) * days;  //테스트는 1분 단위로 나중에 60*24 (일)단위변경

        //JWT 문자열 생성
        String jwtStr = Jwts.builder()
                .setHeader(headers)
                .setClaims(payloads)    //setClaims() : JWT의 페이로드 부분을 설정
                .setIssuedAt(Date.from(ZonedDateTime.now().toInstant()))    //setIssuedAt() : JWT가 발급된 시간을 설정   //ZonedDateTime.now().toInstant()는 현재 시간. Date.from() 메소드는 이를 Date 객체로 변환.
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(time).toInstant()))    //setExpiration() : JWT의 만료 시간을 설정
                .signWith(SignatureAlgorithm.HS256, key.getBytes()) //signWith() : JWT의 서명 알고리즘과 비밀 키를 설정
                .compact(); //설정된 내용을 기반으로 JWT를 최종적으로 생성하여 문자열로 반환

        return jwtStr;
    }


    //토큰을 검증
    public Map<String, Object> validateToken(String token) throws JwtException{

        Map<String, Object> claim = null;

        claim = Jwts.parser()   // parser() : JWT를 파싱하고 검증할 수 있는 파서 인스턴스를 제공    -   JwtParser 객체를 반환
                .setSigningKey(key.getBytes()) // JWT의 서명을 검증할 때 사용
                .parseClaimsJws(token)  //파싱 및 검증, 실패시 에러   -    JWT의 페이로드와 헤더를 추출하고, 서명을 검증합니다. 서명이 유효하지 않거나 토큰이 변조된 경우 JwtException을 던집니다.
                .getBody(); //JWT의 페이로드에 포함된 클레임을 추출    -   Claims 객체를 반환

        return claim;
    }

}
