package com.financecontrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.request.StatementConfirmRequest;
import com.financecontrol.dto.response.ImportResult;
import com.financecontrol.dto.response.ParsedTransactionResponse;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.service.OAuth2UserService;
import com.financecontrol.service.StatementImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(StatementImportController.class)
@AutoConfigureMockMvc(addFilters = false)
class StatementImportControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean StatementImportService                          statementImportService;
    @MockitoBean JwtAuthFilter                                   jwtAuthFilter;
    @MockitoBean OAuth2UserService                               oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler              oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository      cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver        customAuthResolver;

    @Test
    @WithLongPrincipal(1L)
    void preview_arquivoValido_retorna200ComTransacoes() throws Exception {
        ParsedTransactionResponse parsed = new ParsedTransactionResponse(
                "2025-01-15", "Compra Mercado", 150.0, TransactionType.DEBIT,
                1L, "Alimentação", false, List.of());

        MockMultipartFile file = new MockMultipartFile(
                "file", "extrato.csv", "text/csv", "data".getBytes());

        when(statementImportService.previewStatement(eq(1L), any())).thenReturn(List.of(parsed));

        mockMvc.perform(multipart("/api/statements/preview").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Compra Mercado"))
                .andExpect(jsonPath("$[0].amount").value(150.0));
    }

    @Test
    @WithLongPrincipal(1L)
    void preview_arquivoVazio_retorna200ListaVazia() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "vazio.csv", "text/csv", new byte[0]);

        when(statementImportService.previewStatement(eq(1L), any())).thenReturn(List.of());

        mockMvc.perform(multipart("/api/statements/preview").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithLongPrincipal(1L)
    void confirm_requestValido_retorna200ComResultado() throws Exception {
        StatementConfirmRequest req = new StatementConfirmRequest(1L, List.of());
        ImportResult result = new ImportResult(3, "2025-01-01", "2025-01-31");

        when(statementImportService.confirmImport(eq(1L), eq(1L), any())).thenReturn(result);

        mockMvc.perform(post("/api/statements/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported").value(3))
                .andExpect(jsonPath("$.startDate").value("2025-01-01"));
    }
}
