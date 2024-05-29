package com.boot.jwt.security.filter;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

@Log4j2
public class APILoginFilter extends AbstractAuthenticationProcessingFilter {


    public APILoginFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        log.info("APILoginFilter--------------------------------------------");

        //GET 방식 지원X
        if (request.getMethod().equalsIgnoreCase("GET")) {
            log.info("GET METHOD NOT SUPPORT");
            return null;
        }

        Map<String, String> jsonData = parseRequestJSON(request);

        log.info(jsonData);

        //인증 정보
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                jsonData.get("mid"),
                jsonData.get("mpw"));

        //로그인 처리
        return getAuthenticationManager().authenticate(authenticationToken);
    }


    //JSON 데이터를 분석해서 mid, mpw 전달 값을 Map으로 처리
    private Map<String, String> parseRequestJSON(HttpServletRequest request){

        //reader: HTTP 요청 본문을 읽는 Reader 객체
        try(Reader reader = new InputStreamReader(request.getInputStream())) {

            Gson gson = new Gson();

            //reader에서 읽은 JSON 데이터를 Map.class 타입으로 변환합니다. 이때 JSON 객체의 키-값 쌍이 Map<String, String>에 매핑됩니다.
            return gson.fromJson(reader, Map.class);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
