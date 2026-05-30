package com.financecontrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.request.FinnyHistoryRequest;
import com.financecontrol.dto.response.AppNotificationResponse;
import com.financecontrol.enums.AppNotificationType;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.service.AppNotificationService;
import com.financecontrol.service.OAuth2UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(AppNotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AppNotificationControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AppNotificationService                          notificationService;
    @MockitoBean JwtAuthFilter                                   jwtAuthFilter;
    @MockitoBean OAuth2UserService                               oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler              oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository      cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver        customAuthResolver;

    private static AppNotificationResponse notifResp(Long id) {
        return new AppNotificationResponse(id, AppNotificationType.USER_ACTION, null, null,
                null, null, false, "Mensagem " + id, "info", LocalDateTime.now());
    }

    @Test
    @WithLongPrincipal(1L)
    void findAll_retorna200ComLista() throws Exception {
        when(notificationService.findAll(1L)).thenReturn(List.of(notifResp(1L)));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Mensagem 1"));
    }

    @Test
    @WithLongPrincipal(1L)
    void getUnreadCount_retorna200() throws Exception {
        when(notificationService.getUnreadCount(1L)).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    @WithLongPrincipal(1L)
    void markAsRead_retorna204() throws Exception {
        doNothing().when(notificationService).markAsRead(1L, 5L);

        mockMvc.perform(put("/api/notifications/5/read"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithLongPrincipal(1L)
    void markAsRead_naoEncontrado_retorna404() throws Exception {
        doThrow(new ResourceNotFoundException("not found")).when(notificationService).markAsRead(1L, 99L);

        mockMvc.perform(put("/api/notifications/99/read"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithLongPrincipal(1L)
    void markAllAsRead_retorna204() throws Exception {
        doNothing().when(notificationService).markAllAsRead(1L);

        mockMvc.perform(put("/api/notifications/read-all"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithLongPrincipal(1L)
    void saveHistory_retorna200() throws Exception {
        FinnyHistoryRequest req = new FinnyHistoryRequest("Ação do usuário", "info", "/dashboard");
        when(notificationService.saveUserAction(eq(1L), eq("Ação do usuário"), eq("info"), eq("/dashboard")))
                .thenReturn(notifResp(10L));

        mockMvc.perform(post("/api/notifications/history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }
}
