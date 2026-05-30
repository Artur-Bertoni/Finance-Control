package com.financecontrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.request.PasswordChangeRequest;
import com.financecontrol.dto.request.UserRequest;
import com.financecontrol.dto.response.UserResponse;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
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
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean UserService                                     userService;
    @MockitoBean JwtAuthFilter                                   jwtAuthFilter;
    @MockitoBean OAuth2UserService                               oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler              oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository      cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver        customAuthResolver;

    private static UserResponse userResp(Long id, String username) {
        return new UserResponse(id, username, username + "@test.com",
                false, 1, false, "pt", false, LocalDateTime.now(), false, true, true);
    }

    @Test
    void create_endpointPublico_retorna200() throws Exception {
        UserRequest req = new UserRequest("novousuario", "novo@test.com", "senha123", "senha123",
                false, 1, false, "pt");
        when(userService.create(any())).thenReturn(userResp(1L, "novousuario"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("novousuario"));
    }

    @Test
    void create_emailJaExiste_retorna409() throws Exception {
        UserRequest req = new UserRequest("dup", "dup@test.com", "senha123", "senha123",
                false, 1, false, "pt");
        when(userService.create(any())).thenThrow(new BusinessException("error.duplicate.email"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithLongPrincipal(1L)
    void update_requestValido_retorna200() throws Exception {
        UserRequest req = new UserRequest("atualizado", "atualizado@test.com", null, null,
                true, 7, false, "en");
        when(userService.update(eq(1L), any())).thenReturn(userResp(1L, "atualizado"));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("atualizado"));
    }

    @Test
    @WithLongPrincipal(1L)
    void update_usuarioNaoEncontrado_retorna404() throws Exception {
        UserRequest req = new UserRequest("x", "x@test.com", null, null, false, 1, false, "pt");
        when(userService.update(eq(99L), any())).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithLongPrincipal(1L)
    void changePassword_retorna204() throws Exception {
        PasswordChangeRequest req = new PasswordChangeRequest("antiga", "nova123", "nova123");
        doNothing().when(userService).changePassword(eq(1L), any());

        mockMvc.perform(put("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithLongPrincipal(1L)
    void delete_retorna204() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithLongPrincipal(1L)
    void delete_naoEncontrado_retorna404() throws Exception {
        doThrow(new ResourceNotFoundException("not found")).when(userService).delete(99L);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }
}
