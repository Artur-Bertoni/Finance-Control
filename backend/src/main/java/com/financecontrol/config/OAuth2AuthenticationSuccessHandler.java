package com.financecontrol.config;

import com.financecontrol.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthorizationRequest authRequest = (OAuth2AuthorizationRequest) request.getAttribute("_fc_auth_request");
        Long linkUserId = null;

        if (authRequest != null) {
            Object raw = authRequest.getAttributes().get("_fc_link_user_id");
            linkUserId = raw instanceof Long l ? l : null;
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        Object principal = authentication.getPrincipal();
        String providerId;
        String email;
        String name;

        if (principal instanceof OidcUser oidcUser) {
            providerId = oidcUser.getSubject();
            email = oidcUser.getEmail();
            name = oidcUser.getFullName();
        } else if (principal instanceof OAuth2User oauth2User) {
            providerId = "google".equals(provider) ? oauth2User.getAttribute("sub") : oauth2User.getName();
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name");
        } else {
            log.warn("OAuth2 principal type unknown: {}", principal == null ? "null" : principal.getClass());
            getRedirectStrategy().sendRedirect(request, response, "/pages/Login.html");
            return;
        }

        boolean secure = request.isSecure() || baseUrl.startsWith("https");

        if (linkUserId != null) {
            try {
                userService.linkGoogleAccount(linkUserId, provider, providerId);
                getRedirectStrategy().sendRedirect(request, response, "/pages/crud/User.html?linked=true");
            } catch (Exception e) {
                log.warn("Google link failed for userId={}: {}", linkUserId, e.getMessage());
                getRedirectStrategy().sendRedirect(request, response, "/pages/crud/User.html?link_error=true");
            }
            return;
        }

        Long userId = userService.resolveOAuth2Login(provider, providerId, email, name);
        jwtUtil.setTokenCookie(response, jwtUtil.generateToken(userId), secure);
        getRedirectStrategy().sendRedirect(request, response, "/pages/AppShell.html");
    }
}
