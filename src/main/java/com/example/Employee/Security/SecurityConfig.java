package com.example.Employee.Security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final OAuthSuccessHandler oAuthSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/login", "/api/refresh", "/oauth2/**", "/login/**").permitAll()
                .requestMatchers("/api/me").permitAll()
                .requestMatchers("/api/self-block").permitAll()
                .requestMatchers("/api/logout").authenticated()
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html"
                ).permitAll()
                .requestMatchers("/api/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        );
        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("""
                            {
                              "status":401,
                              "error":"Unauthorized",
                              "message":"Authentication required",
                              "path":"%s"
                            }
                            """.formatted(request.getRequestURI()));
                })
        );

        http.addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class).oauth2Login(oauth2 -> oauth2
                .failureHandler(
                        (request, response, exception) ->
                                log.error("OAuth-2 error: {}", exception.getMessage()))
                .successHandler
                        (oAuthSuccessHandler)
        );
        return http.build();
    }
}