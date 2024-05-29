package com.boot.jwt.security.filter;

import com.boot.jwt.security.APIUserDetailService;
import com.boot.jwt.security.exception.AccessTokenException;
import com.boot.jwt.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

//모든 HTTP 요청마다 실행되는 필터입니다. 이 필터는 특정 URL 패턴에 대한 요청만 처리하고, 그 외의 요청은 다음 필터로 넘깁니다. 이 필터의 목적은 요청의 토큰을 검사하는 것입니다.
@Log4j2
@RequiredArgsConstructor
public class TokenCheckFilter extends OncePerRequestFilter {

    private final APIUserDetailService apiUserDetailsService;
    private final JWTUtil jwtUtil;

    @Override       //FilterChain filterChain: 다음 필터를 호출하는 객체입니다.
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!path.startsWith("/api/")) {    // URI가 "/api/"로 시작하지 않으면, 이 요청은 필터링하지 않고 다음 필터로 넘깁니다.
            filterChain.doFilter(request,response);
            return;
        }

        log.info("Token Check Filter....................");
        log.info("JWTUtil: " + jwtUtil);

        try {
            Map<String, Object> payload = validateAccessToken(request);

            //mid
            String mid = (String)payload.get("mid");

            log.info("mid: " + mid);

            UserDetails userDetails = apiUserDetailsService.loadUserByUsername(mid);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            //Spring Security에서 현재 사용자의 인증 정보를 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request,response);
        }catch (AccessTokenException accessTokenException){ //사용자 정의해놓은 예외처리
            accessTokenException.sendResponseError(response);   //sendResponseError() : HttpServletResponse 객체를 사용하여 예외 발생 시 적절한 HTTP 상태 코드와 메시지를 클라이언트에게 전송
        }

    }

    private Map<String, Object> validateAccessToken(HttpServletRequest request) throws AccessTokenException {

        String headerStr = request.getHeader("Authorization");

        if(headerStr == null  || headerStr.length() < 8){
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.UNACCEPT);
        }

        //Bearer 생략
        String tokenType = headerStr.substring(0,6);
        String tokenStr =  headerStr.substring(7);

        if(tokenType.equalsIgnoreCase("Bearer") == false){
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BADTYPE);
        }

        try{
            Map<String, Object> values = jwtUtil.validateToken(tokenStr);

            return values;
        }catch(MalformedJwtException malformedJwtException){
            log.error("MalformedJwtException----------------------");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.MALFORM);
        }catch(SignatureException signatureException){
            log.error("SignatureException----------------------");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BADSIGN);
        }catch(ExpiredJwtException expiredJwtException){
            log.error("ExpiredJwtException----------------------");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.EXPIRED);
        }
    }

}
