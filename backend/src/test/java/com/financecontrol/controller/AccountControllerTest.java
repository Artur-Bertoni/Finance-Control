package com.financecontrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.request.AccountRequest;
import com.financecontrol.dto.response.AccountResponse;
import com.financecontrol.dto.response.FinancialInstitutionResponse;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.service.AccountService;
import com.financecontrol.service.CreditCardInvoiceService;
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
@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AccountService                                  accountService;
    @MockitoBean CreditCardInvoiceService                        creditCardInvoiceService;
    @MockitoBean JwtAuthFilter                                   jwtAuthFilter;
    @MockitoBean OAuth2UserService                               oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler              oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository      cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver        customAuthResolver;

    // ------------------------------------------------------------------ helpers

    private static FinancialInstitutionResponse fiResp() {
        return new FinancialInstitutionResponse(1L, "Nubank", null, null, null, LocalDateTime.now());
    }

    private static AccountResponse accountResp(Long id, String name) {
        return new AccountResponse(id, fiResp(), name, null, null, 500.0, null, null, null, null, LocalDateTime.now());
    }

    // ------------------------------------------------------------------ GET /api/accounts

    @Test
    @WithLongPrincipal(1L)
    void findAll_retorna200ComLista() throws Exception {
        when(accountService.findAllByUser(1L)).thenReturn(List.of(accountResp(1L, "Carteira")));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Carteira"));
    }

    // ------------------------------------------------------------------ GET /api/accounts/{id}

    @Test
    @WithLongPrincipal(1L)
    void findById_encontrado_retorna200() throws Exception {
        when(accountService.findById(1L, 1L)).thenReturn(accountResp(1L, "Poupança"));

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Poupança"));
    }

    @Test
    @WithLongPrincipal(1L)
    void findById_naoEncontrado_retorna404() throws Exception {
        when(accountService.findById(99L, 1L)).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/accounts/99"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ GET /api/accounts/total-value

    @Test
    @WithLongPrincipal(1L)
    void totalValue_retorna200() throws Exception {
        when(accountService.totalValue(1L, null)).thenReturn(1500.0);

        mockMvc.perform(get("/api/accounts/total-value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1500.0));
    }

    // ------------------------------------------------------------------ POST /api/accounts

    @Test
    @WithLongPrincipal(1L)
    void create_requestValido_retorna200() throws Exception {
        AccountRequest req = new AccountRequest(1L, "Nova Conta", null, null, 0.0, null);
        when(accountService.create(eq(1L), any(), eq(false))).thenReturn(accountResp(2L, "Nova Conta"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nova Conta"));
    }

    // ------------------------------------------------------------------ PUT /api/accounts/{id}

    @Test
    @WithLongPrincipal(1L)
    void update_requestValido_retorna200() throws Exception {
        AccountRequest req = new AccountRequest(1L, "Conta Atualizada", null, null, 200.0, null);
        when(accountService.update(eq(1L), any(), any())).thenReturn(accountResp(1L, "Conta Atualizada"));

        mockMvc.perform(put("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Conta Atualizada"));
    }

    // ------------------------------------------------------------------ DELETE /api/accounts/{id}

    @Test
    @WithLongPrincipal(1L)
    void delete_retorna204() throws Exception {
        doNothing().when(accountService).delete(1L, 1L);

        mockMvc.perform(delete("/api/accounts/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithLongPrincipal(1L)
    void delete_naoEncontrado_retorna404() throws Exception {
        doThrow(new ResourceNotFoundException("not found")).when(accountService).delete(99L, 1L);

        mockMvc.perform(delete("/api/accounts/99"))
                .andExpect(status().isNotFound());
    }
}
