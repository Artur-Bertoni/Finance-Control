package com.financecontrol.config;

import com.financecontrol.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2UserService oauth2UserService;
    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;
    private final CookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final CustomOAuth2AuthorizationRequestResolver customAuthorizationRequestResolver;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/verify-email",
                    "/api/auth/resend-verification",
                    "/oauth2/**",
                    "/login/oauth2/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(ep -> ep
                    .authorizationRequestResolver(customAuthorizationRequestResolver)
                    .authorizationRequestRepository(cookieAuthorizationRequestRepository))
                .userInfoEndpoint(ui -> ui.userService(oauth2UserService))
                .successHandler(oauth2SuccessHandler)
                .failureHandler(new SimpleUrlAuthenticationFailureHandler("/pages/Login.html"))
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


}
