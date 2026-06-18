package com.financecontrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.request.TransactionRequest;
import com.financecontrol.dto.response.AccountResponse;
import com.financecontrol.dto.response.AppNotificationResponse;
import com.financecontrol.dto.response.CategoryResponse;
import com.financecontrol.dto.response.FinancialInstitutionResponse;
import com.financecontrol.dto.response.TransactionResponse;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.service.AppNotificationService;
import com.financecontrol.service.OAuth2UserService;
import com.financecontrol.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean TransactionService                              transactionService;
    @MockitoBean AppNotificationService                          notificationService;
    @MockitoBean JwtAuthFilter                                   jwtAuthFilter;
    @MockitoBean OAuth2UserService                               oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler              oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository      cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver        customAuthResolver;

    private static TransactionResponse txResp(Long id) {
        FinancialInstitutionResponse fi = new FinancialInstitutionResponse(1L, "Banco", null, null, null, LocalDateTime.now());
        AccountResponse acc  = new AccountResponse(1L, fi, "Conta", null, null, 0.0, null, null, null, null, LocalDateTime.now());
        CategoryResponse cat = new CategoryResponse(1L, "Cat", null, null, List.of(), LocalDateTime.now());
        return new TransactionResponse(id, acc, cat, null, 100.0,
                LocalDate.of(2025, 1, 15), TransactionType.DEBIT, 0, null, 0L, null, null, null, null, LocalDateTime.now());
    }

    private static TransactionRequest txReq() {
        return new TransactionRequest(1L, 1L, null, 100.0,
                LocalDate.of(2025, 1, 15), TransactionType.DEBIT, 0, null, null);
    }

    @Test
    @WithLongPrincipal(1L)
    void findAll_retorna200ComLista() throws Exception {
        when(transactionService.findAllByUser(eq(1L), any(), any(), any(), any()))
                .thenReturn(List.of(txResp(10L)));

        mockMvc.perform(get("/api/transactions")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    @WithLongPrincipal(1L)
    void findById_encontrado_retorna200() throws Exception {
        when(transactionService.findById(10L)).thenReturn(txResp(10L));

        mockMvc.perform(get("/api/transactions/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithLongPrincipal(1L)
    void findById_naoEncontrado_retorna404() throws Exception {
        when(transactionService.findById(99L)).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/transactions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithLongPrincipal(1L)
    void create_requestValido_retorna200() throws Exception {
        TransactionResponse tx = txResp(20L);
        when(transactionService.create(eq(1L), any(), eq(false))).thenReturn(tx);
        when(notificationService.checkGoalImpact(1L, 20L)).thenReturn(List.of());

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txReq())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction.id").value(20));
    }

    @Test
    @WithLongPrincipal(1L)
    void create_retornaNotificacoesDeImpacto() throws Exception {
        TransactionResponse tx = txResp(20L);
        AppNotificationResponse notif = new AppNotificationResponse(
                1L, null, 1L, "Meta Viagem", 20L, null, false, "Meta atingida!", "info", LocalDateTime.now());

        when(transactionService.create(eq(1L), any(), eq(false))).thenReturn(tx);
        when(notificationService.checkGoalImpact(1L, 20L)).thenReturn(List.of(notif));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txReq())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications[0].message").value("Meta atingida!"));
    }

    @Test
    @WithLongPrincipal(1L)
    void update_requestValido_retorna200() throws Exception {
        when(transactionService.update(eq(10L), eq(1L), any())).thenReturn(txResp(10L));

        mockMvc.perform(put("/api/transactions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txReq())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @WithLongPrincipal(1L)
    void delete_retorna204() throws Exception {
        doNothing().when(transactionService).delete(10L);

        mockMvc.perform(delete("/api/transactions/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithLongPrincipal(1L)
    void delete_naoEncontrado_retorna404() throws Exception {
        doThrow(new ResourceNotFoundException("not found")).when(transactionService).delete(99L);

        mockMvc.perform(delete("/api/transactions/99"))
                .andExpect(status().isNotFound());
    }
}
