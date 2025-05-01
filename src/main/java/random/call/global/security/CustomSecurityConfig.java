package random.call.global.security;


import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import random.call.global.jwt.JwtAuthFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class CustomSecurityConfig {

    private final static String[] PERMIT_ALL_URI_MEMBER = {
            "/api/v1/member/sign-up",
            "/api/v1/member/sign-in",
            "/api/v1/member/check-nickname",
            "/api/v1/member/check-username",
    };


    private final static String[] PERMIT_ALL_URI_PARTY = {
            "/api/v1/party/**",
            "/ws-chat/**",
            "/ws/**",
            "/app/**",
            "/ws/**",
            "/topic/**",
            "/app/**",
            "/hello",
            "/ws-stomp/**"
    };


    private final JwtAuthFilter jwtAuthFilter;

    private final EndpointHandler endpointHandler;


    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(request -> {

                    // 기타 허용 URI (멤버 관련)
                    String[] permitAllMemberUri = Stream.of(PERMIT_ALL_URI_MEMBER,PERMIT_ALL_URI_PARTY)
                            .flatMap(Arrays::stream)
                            .toArray(String[]::new);

                    request.requestMatchers(permitAllMemberUri).permitAll()
//                    .requestMatchers("/ws/**", "/topic/**", "/app/**").permitAll() // 웹소켓 경로 전체 허용
                    ;

                    // 에러 페이지 허용
                    request.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();

                    // 나머지 요청은 인증 필요
                    request.anyRequest().authenticated();
                })
                .exceptionHandling(configure -> {
                    configure
                            .authenticationEntryPoint(endpointHandler)
                            .accessDeniedHandler(endpointHandler);
                })
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.addAllowedOrigin("*"); // React Native 앱 허용

//        configuration.addAllowedOrigin("http://localhost:8081");
//        configuration.addAllowedOrigin("http://10.0.2.2:8081");
//        configuration.addAllowedOrigin("https://ff14.vercel.app/"); // 배포된 프론트

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);

        configuration.setAllowedHeaders(List.of("Access-Token","Authorization", "Content-Type","Refresh-Token"));
        configuration.setExposedHeaders(List.of("Access-Token","Authorization","Refresh-Token"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}
