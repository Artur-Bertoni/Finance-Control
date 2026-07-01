package com.financecontrol.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;
    private final JwtUtil jwtUtil;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository,
                                                    JwtUtil jwtUtil) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
        this.jwtUtil = jwtUtil;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return customize(request, defaultResolver.resolve(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, 
                                              String clientRegistrationId) {
        return customize(request, defaultResolver.resolve(request, clientRegistrationId));
    }

    private OAuth2AuthorizationRequest customize(HttpServletRequest request,
                                                 OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) return null;

        if (!"true".equals(request.getParameter("link"))) return authorizationRequest;

        Long userId = extractUserIdFromJwt(request);
        if (userId == null) return authorizationRequest;

        return OAuth2AuthorizationRequest.from(authorizationRequest).attributes(attrs -> attrs.put("_fc_link_user_id", userId)).build();
    }

    private Long extractUserIdFromJwt(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromCookie(request);

        if (token == null || !jwtUtil.isValid(token)) return null;
        
        try {
            return jwtUtil.extractUserId(token);
        } catch (Exception e) {
            return null;
        }
    }
}
