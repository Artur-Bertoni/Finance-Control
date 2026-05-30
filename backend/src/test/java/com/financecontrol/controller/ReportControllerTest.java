package com.financecontrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.response.DashboardResponse;
import com.financecontrol.service.OAuth2UserService;
import com.financecontrol.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean ReportService                                   reportService;
    @MockitoBean JwtAuthFilter                                   jwtAuthFilter;
    @MockitoBean OAuth2UserService                               oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler              oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository      cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver        customAuthResolver;

    @Test
    @WithLongPrincipal(1L)
    void getDashboard_retorna200() throws Exception {
        DashboardResponse dashboard = new DashboardResponse(List.of(), List.of(), List.of(), List.of());
        when(reportService.getDashboard(eq(1L), any(), any(), any())).thenReturn(dashboard);

        mockMvc.perform(get("/api/reports/dashboard")
                        .param("startDate", "2025-01-01")
                        .param("endDate",   "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyData").isArray());
    }

    @Test
    @WithLongPrincipal(1L)
    void getDashboard_comFiltroAccount_retorna200() throws Exception {
        DashboardResponse dashboard = new DashboardResponse(List.of(), List.of(), List.of(), List.of());
        when(reportService.getDashboard(eq(1L), any(), any(), eq(5L))).thenReturn(dashboard);

        mockMvc.perform(get("/api/reports/dashboard")
                        .param("startDate", "2025-01-01")
                        .param("endDate",   "2025-01-31")
                        .param("accountId", "5"))
                .andExpect(status().isOk());
    }
}
