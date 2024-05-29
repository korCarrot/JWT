package com.boot.jwt.util;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
@Log4j2
public class JWTUtilTests {

    @Autowired
    private JWTUtil jwtUtil;

    //JWT 토큰 생성
    @Test
    public void testGenerate(){

        Map<String, Object> claimMap = Map.of("mid", "ABCDE");

        String jwtStr = jwtUtil.generateToken(claimMap, 1);

        log.info(jwtStr);

    }

    //JWT 토큰 검증
    //ExpiredJwtException : 유효기간 만료 / SignatureException : 서명 부분 문자열 예외
    @Test
    public void testValidate(){

        //유효 시간이 지난 토큰
        String jwtStr = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MTY4ODMyMjQsIm1pZCI6IkFCQ0RFIiwiaWF0IjoxNzE2ODgzMTY0fQ.I1dUialFzTEr6A5-1pKbMgaOzvTOXQAk5IpydHxnWOE";

        Map<String, Object> claim = jwtUtil.validateToken(jwtStr);

        log.info(claim);

    }


    //JWT 토큰 생성 및 검증
    @Test
    public void testAll(){

                                                //Map의 값은 payload에 들어간다.
        String jwtStr = jwtUtil.generateToken(Map.of("mid", "AAAA", "email", "aaaa@bbb.com"), 1);

        log.info(jwtStr);

        Map<String, Object> claim = jwtUtil.validateToken(jwtStr);

        log.info("MID: "+claim.get("mid"));
        log.info("EMAIL: " + claim.get("email"));
    }
}
