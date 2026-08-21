package com.financecontrol.controller;

import tools.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.JwtUtil;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.request.UserSettingsRequest;
import com.financecontrol.dto.response.UserSettingsResponse;
import com.financecontrol.service.OAuth2UserService;
import com.financecontrol.service.UserSettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserSettingsController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserSettingsControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean UserSettingsService                             userSettingsService;
    @MockitoBean JwtUtil                                         jwtUtil;
    @MockitoBean JwtAuthFilter                                   jwtAuthFilter;
    @MockitoBean OAuth2UserService                               oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler              oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository      cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver        customAuthResolver;

    @Test
    @WithLongPrincipal(1L)
    void find_retornaConfiguracoesDoUsuarioLogado() throws Exception {
        when(userSettingsService.find(1L)).thenReturn(UserSettingsResponse.defaults());

        mockMvc.perform(get("/api/user-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportsEnabled").value(true))
                .andExpect(jsonPath("$.emailsEnabled").value(true));
    }

    @Test
    void find_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(get("/api/user-settings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithLongPrincipal(1L)
    void update_aplicaTogglesDoUsuarioLogado() throws Exception {
        UserSettingsRequest req = new UserSettingsRequest(false, null, false, null, null, null, null, null);
        when(userSettingsService.update(eq(1L), any()))
                .thenReturn(new UserSettingsResponse(false, true, false, true, true, true, true, true));

        mockMvc.perform(put("/api/user-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportsEnabled").value(false))
                .andExpect(jsonPath("$.goalsEnabled").value(false))
                .andExpect(jsonPath("$.budgetsEnabled").value(true));
    }
}
