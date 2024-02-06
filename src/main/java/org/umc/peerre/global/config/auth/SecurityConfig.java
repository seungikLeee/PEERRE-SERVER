package org.umc.peerre.global.config.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RememberMeConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.umc.peerre.domain.user.repository.UserRepository;
import org.umc.peerre.global.config.CorsConfig;
import org.umc.peerre.global.config.auth.jwt.JwtAuthenticationEntryPoint;
import org.umc.peerre.global.config.auth.jwt.JwtProvider;
import org.umc.peerre.global.config.auth.principal.PrincipalOAuth2UserService;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CorsConfig corsConfig;
    private final JwtProvider jwtProvider;
    private final PrincipalOAuth2UserService principalOauth2UserService;
    private final OAuthAuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final ExceptionHandlerFilter exceptionHandlerFilter;
    private final UserRepository userRepository;

    /* 사용 X
    private static final String[] whiteList = {"/",
            "api/user/test",
            "api/user/token/**",
            };

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(whiteList);
    }
*/
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .rememberMe(RememberMeConfigurer::disable)
                .sessionManagement(sessionManagementConfigurer ->
                        sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(LoginConfigurer -> LoginConfigurer
                        .userInfoEndpoint(endpointConfig -> endpointConfig.userService(principalOauth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler))
                .exceptionHandling(exceptionHandlingConfigurer ->
                        exceptionHandlingConfigurer.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilter(corsConfig.corsFilter())
                .addFilterBefore(new JwtAuthenticationFilter(userRepository,jwtProvider), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(exceptionHandlerFilter, JwtAuthenticationFilter.class);
        //접근 허용 uri 추가
        http
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/", "api/user/test,", "/test").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}