package com.financecontrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.response.ChangeGroupResponse;
import com.financecontrol.service.HistoryService;
import com.financecontrol.service.OAuth2UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class HistoryControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean HistoryService                                  changeHistoryService;
    @MockitoBean JwtAuthFilter                                   jwtAuthFilter;
    @MockitoBean OAuth2UserService                               oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler              oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository      cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver        customAuthResolver;

    @Test
    @WithLongPrincipal(1L)
    void getHistory_retorna200ComGrupos() throws Exception {
        ChangeGroupResponse group = new ChangeGroupResponse(
                UUID.randomUUID().toString(), LocalDateTime.now(), true, false, List.of());

        when(changeHistoryService.getHistory("transaction", 10L)).thenReturn(List.of(group));

        mockMvc.perform(get("/api/change-history/transaction/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].creation").value(true));
    }

    @Test
    @WithLongPrincipal(1L)
    void getHistory_semRegistros_retorna200ListaVazia() throws Exception {
        when(changeHistoryService.getHistory("account", 99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/change-history/account/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
