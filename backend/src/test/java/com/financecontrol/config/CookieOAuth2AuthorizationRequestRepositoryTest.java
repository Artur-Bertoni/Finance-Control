package com.financecontrol.config;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static org.assertj.core.api.Assertions.*;

@SuppressWarnings("null")
class CookieOAuth2AuthorizationRequestRepositoryTest {

    private final CookieOAuth2AuthorizationRequestRepository repo =
            new CookieOAuth2AuthorizationRequestRepository();

    private OAuth2AuthorizationRequest sampleRequest() {
        return OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .clientId("client-id")
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .scope("openid", "email")
                .state("state-123")
                .build();
    }

    @Test
    void saveThenLoad_roundTripViaCookie() {
        OAuth2AuthorizationRequest original = sampleRequest();
        MockHttpServletRequest saveReq = new MockHttpServletRequest();
        MockHttpServletResponse saveResp = new MockHttpServletResponse();

        repo.saveAuthorizationRequest(original, saveReq, saveResp);

        Cookie cookie = saveResp.getCookie("oauth2_auth_request");
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isNotEmpty();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(300);

        MockHttpServletRequest loadReq = new MockHttpServletRequest();
        loadReq.setCookies(cookie);

        OAuth2AuthorizationRequest loaded = repo.loadAuthorizationRequest(loadReq);

        assertThat(loaded).isNotNull();
        assertThat(loaded.getClientId()).isEqualTo("client-id");
        assertThat(loaded.getState()).isEqualTo("state-123");
        assertThat(loaded.getAuthorizationUri()).isEqualTo(original.getAuthorizationUri());
    }

    @Test
    void loadAuthorizationRequest_semCookies_retornaNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThat(repo.loadAuthorizationRequest(request)).isNull();
    }

    @Test
    void loadAuthorizationRequest_cookieInvalido_retornaNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("oauth2_auth_request", "not-valid-base64-object"));

        assertThat(repo.loadAuthorizationRequest(request)).isNull();
    }

    @Test
    void saveAuthorizationRequest_nullComCookieExistente_apagaCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("oauth2_auth_request", "anything"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        repo.saveAuthorizationRequest(null, request, response);

        Cookie deleted = response.getCookie("oauth2_auth_request");
        assertThat(deleted).isNotNull();
        assertThat(deleted.getMaxAge()).isZero();
        assertThat(deleted.getValue()).isEmpty();
    }

    @Test
    void removeAuthorizationRequest_retornaRequestEApagaCookie() {
        OAuth2AuthorizationRequest original = sampleRequest();
        MockHttpServletResponse saveResp = new MockHttpServletResponse();
        repo.saveAuthorizationRequest(original, new MockHttpServletRequest(), saveResp);
        Cookie cookie = saveResp.getCookie("oauth2_auth_request");

        MockHttpServletRequest removeReq = new MockHttpServletRequest();
        removeReq.setCookies(cookie);
        MockHttpServletResponse removeResp = new MockHttpServletResponse();

        OAuth2AuthorizationRequest removed = repo.removeAuthorizationRequest(removeReq, removeResp);

        assertThat(removed).isNotNull();
        assertThat(removed.getClientId()).isEqualTo("client-id");
        assertThat(removeReq.getAttribute("_fc_auth_request")).isEqualTo(removed);

        Cookie deleted = removeResp.getCookie("oauth2_auth_request");
        assertThat(deleted).isNotNull();
        assertThat(deleted.getMaxAge()).isZero();
    }

    @Test
    void removeAuthorizationRequest_semCookie_retornaNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(repo.removeAuthorizationRequest(request, response)).isNull();
    }
}
