package com.boot.jwt.config;

import com.boot.jwt.security.APIUserDetailService;
import com.boot.jwt.security.filter.APILoginFilter;
import com.boot.jwt.security.filter.RefreshTokenFilter;
import com.boot.jwt.security.filter.TokenCheckFilter;
import com.boot.jwt.security.handler.APILoginSuccessHandler;
import com.boot.jwt.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Log4j2
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true) //메소드 수준의 보안 설정을 활성화하는 역할  / prePostEnabled 를 true로 설정 시 -> @PreAuthorize와 @PostAuthorize 어노테이션을 사용하여 사전 혹은 사후의 권한을 체크할 수 있습니다. 메소드 수준의 보안을 활성화할 수 있습니다.
//@PreAuthorize: 메소드 실행 전에 특정 조건을 검사하여 접근을 허용 또는 거부합니다.  /  @PostAuthorize: 메소드 실행 후에 특정 조건을 검사하여 결과를 반환하거나 변경합니다.
public class CustomSecurityConfig {

    private final APIUserDetailService apiUserDetailService;

    private final JWTUtil jwtUtil;

//  스프링은 기본적으로 빈을 싱글톤으로 관리합니다. 따라서 passwordEncoder() 메서드를 통해 생성된 BCryptPasswordEncoder 객체는 Spring IoC 컨테이너에 의해 단 한 번만 생성되고, 그 후에는 해당 빈이 필요한 곳에서 재사용됩니다.
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

// SecurityFilterChain : HttpSecurity 객체를 사용하여 보안 필터 체인을 구성합니다. 모든 요청을 인증하도록 설정하고, 사용자 정의 로그인 페이지와 로그아웃 기능을 설정합니다.
//   HTTP 요청 인증 및 인가: 요청을 인증하고 인가하는 필터를 설정합니다.
//   로그인/로그아웃: 로그인 페이지, 로그인 처리, 로그아웃 처리 등을 설정합니다.
//   보안 헤더 설정: XSS, CSRF 등의 공격을 방지하기 위한 보안 헤더를 설정합니다.


    //  WebSecurityCustomizer : 보안 필터 체인을 구성하는 대신, 보안 설정에서 특정 요청을 완전히 무시하도록 설정하는 역할. 보안 필터 체인에 전혀 포함되지 않으며, 보안 검사를 받지 않습니다.
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){

        log.info("---------web configure ---------");

        return web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//web.ignoring() : (특정 요청을 보안 필터 체인에서 제외하도록 설정) 지정된 경로에 대한 요청이 보안 필터 체인을 통과하지 않고 무시됩니다.
// requestMatchers : 무시할 요청 매처를 지정
// PathRequest.toStaticResources().atCommonLocations() : 일반적으로 사용되는 정적 리소스 경로를 포함합니다
    }


    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http)throws Exception{

        log.info("----------configure------------");

        //AuthenticationManagerBuilder : 사용자 정의 인증 매니저를 설정하고 빌드하는 데 사용
        //getSharedObject(Class<T> sharedType) : HttpSecurity와 같은 보안 구성 객체는 내부적으로 여러 구성 요소를 *공유 객체로 관리. 해당 타입의 공유 객체를 반환
        //*공유 객체 : HttpSecurity 객체를 구성하는 동안 설정된 여러 구성 요소들. 다른 보안 설정에서 재사용될 수 있도록 공유되는 객체들
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(apiUserDetailService)   //  UserDetailsService() : 사용자 정보를 가져오는 방법을 정의
                .passwordEncoder(passwordEncoder());

        // Get AuthenticationManager / AuthenticationManager 설정 - 로그인시 실제 인증 처리를 담당
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
        //사용자 정의 AuthenticationManager : 모든 인증 요청이 이 매니저를 통해 처리
        http.authenticationManager(authenticationManager);

        // APILoginFilter
        //defaultFilterProcessesUrl 매개변수 :  필터가 처리할 기본 URL 패턴을 설정
        APILoginFilter apiLoginFilter = new APILoginFilter("/generateToken");
        apiLoginFilter.setAuthenticationManager(authenticationManager); //인증 매니저를 사용하여 인증 요청을 처리. 필터가 사용할 AuthenticationManager를 설정

        //APILoginSuccessHandler
        APILoginSuccessHandler successHandler = new APILoginSuccessHandler(jwtUtil);
        //SuccessHandler 세팅
        apiLoginFilter.setAuthenticationSuccessHandler(successHandler);

        // APILoginFilter의 위치 조정 - 커스텀 필터를 스프링 시큐리티 필터 체인에 추가하고, 특정 필터 앞에 배치
        // 로그인 요청을 처리하는 커스텀 필터가 UsernamePasswordAuthenticationFilter보다 먼저 실행
        http.addFilterBefore(apiLoginFilter, UsernamePasswordAuthenticationFilter.class);

        //api로 시작하는 모든 경로는 TokenCheckFilter 동작
        http.addFilterBefore(
                tokenCheckFilter(jwtUtil, apiUserDetailService), UsernamePasswordAuthenticationFilter.class
        );

        //refreshToken 호출 처리 - 다른 JWT 관련 필터들의 동작 이전으로 배치
        http.addFilterBefore(new RefreshTokenFilter("/refreshToken", jwtUtil), TokenCheckFilter.class);

        //CSRF 토큰 비활성화
        http.csrf().disable();  // 1. CSRF토큰 비활성화

        //sessionManagement() : 세션 관리 설정을 시작하는 데 사용. 세션 관리 정책을 정의하고, 세션 고정 보호, 동시 세션 제어, 세션 만료 등의 다양한 세션 관련 설정을 구성하는 데 사용
        //sessionCreationPolicy() : 세션 생성 정책을 설정. SessionCreationPolicy 열거형(Enum)을 인수로 받아, 스프링 시큐리티가 세션을 어떻게 생성하고 관리할지를 정의
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);    // 2. 세션을 사용하지 않음

        //CORS 관련 설정
        http.cors(httpSecurityCorsConfigurer -> {
            httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource());
        });

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); //허용된 출처 패턴을 설정
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE")); //허용된 HTTP 메서드를 설정
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));   //허용된 HTTP 헤더를 설정
        configuration.setAllowCredentials(true);    //요청이 인증 정보를 포함할 수 있는지 여부를 설정
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); //CORS 정책을 설정하고 관리하는 데 사용 -> 특정 URL 패턴에 대한 CORS 구성을 등록
        source.registerCorsConfiguration("/**", configuration); //모든 URL에 대해 CORS 구성을 적용
        return source;
    }

    private TokenCheckFilter tokenCheckFilter(JWTUtil jwtUtil, APIUserDetailService apiUserDetailService){
        return new TokenCheckFilter(apiUserDetailService, jwtUtil); //@RequiredArgsConstructor가 생성자를 만들기 때문
    }

}