package com.financecontrol.config;

import com.financecontrol.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock JwtUtil     jwtUtil;
    @Mock UserService userService;

    OAuth2AuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OAuth2AuthenticationSuccessHandler(jwtUtil, userService);
        ReflectionTestUtils.setField(handler, "baseUrl", "http://localhost:8080");
    }

    @Test
    void onAuthenticationSuccess_oauth2User_novoLogin_gravaCookieERedireciona() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("sub")).thenReturn("sub123");
        when(principal.getAttribute("email")).thenReturn("user@test.com");
        when(principal.getAttribute("name")).thenReturn("User Test");

        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        when(token.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(token.getPrincipal()).thenReturn(principal);

        when(userService.resolveOAuth2Login("google", "sub123", "user@test.com", "User Test")).thenReturn(7L);
        when(jwtUtil.generateToken(7L)).thenReturn("jwt-token");

        handler.onAuthenticationSuccess(req, res, token);

        verify(jwtUtil).setTokenCookie(res, "jwt-token", false);
        assertThat(res.getRedirectedUrl()).isEqualTo("/pages/AppShell.html#token=jwt-token");
    }

    @Test
    void onAuthenticationSuccess_oidcUser_resolveLogin() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        OidcUser principal = mock(OidcUser.class);
        when(principal.getSubject()).thenReturn("oidc-sub");
        when(principal.getEmail()).thenReturn("oidc@test.com");
        when(principal.getFullName()).thenReturn("Oidc User");

        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        when(token.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(token.getPrincipal()).thenReturn(principal);

        when(userService.resolveOAuth2Login("google", "oidc-sub", "oidc@test.com", "Oidc User")).thenReturn(8L);
        when(jwtUtil.generateToken(8L)).thenReturn("jwt8");

        handler.onAuthenticationSuccess(req, res, token);

        verify(jwtUtil).setTokenCookie(res, "jwt8", false);
        assertThat(res.getRedirectedUrl()).isEqualTo("/pages/AppShell.html#token=jwt8");
    }

    @Test
    void onAuthenticationSuccess_linkUserId_vinculaContaERedireciona() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        OAuth2AuthorizationRequest authRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .clientId("cid")
                .redirectUri("http://localhost/callback")
                .attributes(attrs -> attrs.put("_fc_link_user_id", 99L))
                .build();
        req.setAttribute("_fc_auth_request", authRequest);

        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("sub")).thenReturn("sub99");
        when(principal.getAttribute("email")).thenReturn("link@test.com");
        when(principal.getAttribute("name")).thenReturn("Link User");

        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        when(token.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(token.getPrincipal()).thenReturn(principal);

        handler.onAuthenticationSuccess(req, res, token);

        verify(userService).linkGoogleAccount(99L, "google", "sub99");
        assertThat(res.getRedirectedUrl()).isEqualTo("/pages/crud/User.html?linked=true");
        verify(jwtUtil, never()).setTokenCookie(any(), any(), anyBoolean());
    }

    @Test
    void onAuthenticationSuccess_linkFalha_redirecionaComErro() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        OAuth2AuthorizationRequest authRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .clientId("cid")
                .redirectUri("http://localhost/callback")
                .attributes(attrs -> attrs.put("_fc_link_user_id", 99L))
                .build();
        req.setAttribute("_fc_auth_request", authRequest);

        OAuth2User principal = mock(OAuth2User.class);
        when(principal.getAttribute("sub")).thenReturn("sub99");
        when(principal.getAttribute("email")).thenReturn("link@test.com");
        when(principal.getAttribute("name")).thenReturn("Link User");

        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        when(token.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(token.getPrincipal()).thenReturn(principal);

        doThrow(new RuntimeException("já vinculada")).when(userService).linkGoogleAccount(99L, "google", "sub99");

        handler.onAuthenticationSuccess(req, res, token);

        assertThat(res.getRedirectedUrl()).isEqualTo("/pages/crud/User.html?link_error=true");
    }

}
