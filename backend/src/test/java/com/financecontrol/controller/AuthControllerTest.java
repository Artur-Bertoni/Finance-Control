package com.financecontrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.JwtUtil;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.request.LoginRequest;
import com.financecontrol.dto.response.UserResponse;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.service.OAuth2UserService;
import com.financecontrol.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean UserService                                     userService;
    @MockitoBean JwtUtil                                         jwtUtil;
    @MockitoBean JwtAuthFilter                                   jwtAuthFilter;
    @MockitoBean OAuth2UserService                               oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler              oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository      cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver        customAuthResolver;

    private static UserResponse userResp(Long id) {
        return new UserResponse(id, "testuser", "test@test.com",
                false, 1, false, "pt", false, LocalDateTime.now(), false, true, true);
    }

    @Test
    void login_credenciaisValidas_retorna200() throws Exception {
        LoginRequest req = new LoginRequest("test@test.com", "senha123");
        when(userService.login("test@test.com", "senha123")).thenReturn(userResp(1L));
        when(jwtUtil.generateToken(1L)).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void login_credenciaisInvalidas_retorna409() throws Exception {
        LoginRequest req = new LoginRequest("wrong@test.com", "errado");
        when(userService.login("wrong@test.com", "errado"))
                .thenThrow(new BusinessException("error.auth.invalid"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithLongPrincipal(1L)
    void me_retorna200ComUsuario() throws Exception {
        when(userService.findById(1L)).thenReturn(userResp(1L));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void logout_retorna200() throws Exception {
        doNothing().when(jwtUtil).clearTokenCookie(any());

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());
    }

    @Test
    void verifyEmail_tokenValido_retorna302() throws Exception {
        when(userService.verifyEmail("valid-token")).thenReturn(1L);
        when(jwtUtil.generateToken(1L)).thenReturn("fake-jwt");

        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", "valid-token"))
                .andExpect(status().isFound());
    }

    @Test
    void verifyEmail_tokenInvalido_retorna409() throws Exception {
        when(userService.verifyEmail("bad-token"))
                .thenThrow(new BusinessException("error.token.invalid"));

        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", "bad-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resendVerification_retorna200() throws Exception {
        doNothing().when(userService).resendVerification("test@test.com");

        mockMvc.perform(post("/api/auth/resend-verification")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk());
    }

    @Test
    @WithLongPrincipal(1L)
    void linkGoogle_retorna200ComRedirectUrl() throws Exception {
        mockMvc.perform(post("/api/auth/link/google"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectUrl").value("/oauth2/authorization/google?link=true"));
    }

    @Test
    @WithLongPrincipal(1L)
    void unlinkGoogle_retorna204() throws Exception {
        doNothing().when(userService).unlinkGoogle(1L);

        mockMvc.perform(delete("/api/auth/link/google"))
                .andExpect(status().isNoContent());
    }
}
