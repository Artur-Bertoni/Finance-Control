package com.financecontrol.config;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomOAuth2AuthorizationRequestResolverTest {

    private ClientRegistrationRepository clientRegistrationRepository;
    private JwtUtil jwtUtil;
    private CustomOAuth2AuthorizationRequestResolver resolver;

    private static final String SECRET =
            "test-secret-key-for-testing-only-must-be-at-least-64-chars-long!!!!!";

    private ClientRegistration googleRegistration() {
        return ClientRegistration.withRegistrationId("google")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .scope("openid", "email", "profile")
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();
    }

    @BeforeEach
    void setUp() {
        clientRegistrationRepository = mock(ClientRegistrationRepository.class);
        jwtUtil = new JwtUtil(SECRET, 1);
        resolver = new CustomOAuth2AuthorizationRequestResolver(clientRegistrationRepository, jwtUtil);
    }

    @Test
    void resolve_caminhoNaoOauth2_retornaNull() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/transactions");

        assertThat(resolver.resolve(request)).isNull();
    }

    @Test
    void resolve_caminhoOauth2SemLink_retornaRequestSemAtributoCustom() {
        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(googleRegistration());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/google");

        OAuth2AuthorizationRequest result = resolver.resolve(request);

        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo("client-id");
        assertThat(result.getAttributes()).doesNotContainKey("_fc_link_user_id");
    }

    @Test
    void resolve_linkSemTokenValido_naoAdicionaUserId() {
        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(googleRegistration());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/google");
        request.setParameter("link", "true");

        OAuth2AuthorizationRequest result = resolver.resolve(request);

        assertThat(result).isNotNull();
        assertThat(result.getAttributes()).doesNotContainKey("_fc_link_user_id");
    }

    @Test
    void resolve_linkComTokenValido_adicionaUserId() {
        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(googleRegistration());

        String token = jwtUtil.generateToken(99L);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/google");
        request.setParameter("link", "true");
        request.setCookies(new Cookie(JwtUtil.COOKIE_NAME, token));

        OAuth2AuthorizationRequest result = resolver.resolve(request);

        assertThat(result).isNotNull();
        assertThat(result.getAttributes()).containsEntry("_fc_link_user_id", 99L);
    }

    @Test
    void resolveComRegistrationId_semLink_retornaRequest() {
        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(googleRegistration());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/google");

        OAuth2AuthorizationRequest result = resolver.resolve(request, "google");

        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo("client-id");
        assertThat(result.getAttributes()).doesNotContainKey("_fc_link_user_id");
    }
}
