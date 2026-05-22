package com.financecontrol.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;

@Component
public class CookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String COOKIE_NAME = "oauth2_auth_request";
    private static final int    MAX_AGE     = 300;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookieValue(request);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(request, response);
            return;
        }

        String value;

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(authorizationRequest);
            value = Base64.getUrlEncoder().withoutPadding().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            return;
        }

        boolean secure = request.isSecure();
        Cookie cookie = new Cookie(COOKIE_NAME, value);

        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(MAX_AGE);
        cookie.setSecure(secure);
        response.addCookie(cookie);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest auth = getCookieValue(request);

        if (auth != null) {
            deleteCookie(request, response);
            request.setAttribute("_fc_auth_request", auth);
        }

        return auth;
    }

    private OAuth2AuthorizationRequest getCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .map(c -> {
                    try {
                        byte[] bytes = Base64.getUrlDecoder().decode(c.getValue());

                        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                            return (OAuth2AuthorizationRequest) ois.readObject();
                        }
                    } catch (Exception e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    private void deleteCookie(HttpServletRequest request, 
                              HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        
        if (cookies == null) return;

        Arrays.stream(cookies)
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .ifPresent(c -> {
                    Cookie blank = new Cookie(COOKIE_NAME, "");
                    blank.setPath("/");
                    blank.setHttpOnly(true);
                    blank.setMaxAge(0);
                    blank.setSecure(c.getSecure());
                    response.addCookie(blank);
                });
    }
}
