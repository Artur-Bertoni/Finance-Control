package com.financecontrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.request.TransactionLocaleRequest;
import com.financecontrol.dto.response.TransactionLocaleResponse;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.service.OAuth2UserService;
import com.financecontrol.service.TransactionLocaleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(TransactionLocaleController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionLocaleControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean TransactionLocaleService                        transactionLocaleService;
    @MockitoBean JwtAuthFilter                                   jwtAuthFilter;
    @MockitoBean OAuth2UserService                               oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler              oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository      cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver        customAuthResolver;

    // ------------------------------------------------------------------ helpers

    private static TransactionLocaleResponse localeResp(Long id, String name) {
        return new TransactionLocaleResponse(id, name, null, null);
    }

    // ------------------------------------------------------------------ GET /api/transaction-locales

    @Test
    @WithLongPrincipal(1L)
    void findAll_retorna200ComLista() throws Exception {
        when(transactionLocaleService.findAllByUser(1L)).thenReturn(List.of(localeResp(1L, "Mercado")));

        mockMvc.perform(get("/api/transaction-locales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mercado"));
    }

    // ------------------------------------------------------------------ GET /api/transaction-locales/{id}

    @Test
    @WithLongPrincipal(1L)
    void findById_encontrado_retorna200() throws Exception {
        when(transactionLocaleService.findById(1L, 1L)).thenReturn(localeResp(1L, "Farmácia"));

        mockMvc.perform(get("/api/transaction-locales/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Farmácia"));
    }

    @Test
    @WithLongPrincipal(1L)
    void findById_naoEncontrado_retorna404() throws Exception {
        when(transactionLocaleService.findById(99L, 1L)).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/transaction-locales/99"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ POST /api/transaction-locales

    @Test
    @WithLongPrincipal(1L)
    void create_requestValido_retorna200() throws Exception {
        TransactionLocaleRequest req = new TransactionLocaleRequest("Restaurante", null, null);
        when(transactionLocaleService.create(eq(1L), any(), eq(false))).thenReturn(localeResp(2L, "Restaurante"));

        mockMvc.perform(post("/api/transaction-locales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Restaurante"));
    }

    // ------------------------------------------------------------------ PUT /api/transaction-locales/{id}

    @Test
    @WithLongPrincipal(1L)
    void update_requestValido_retorna200() throws Exception {
        TransactionLocaleRequest req = new TransactionLocaleRequest("Restaurante VIP", null, null);
        when(transactionLocaleService.update(eq(1L), eq(1L), any())).thenReturn(localeResp(1L, "Restaurante VIP"));

        mockMvc.perform(put("/api/transaction-locales/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Restaurante VIP"));
    }

    // ------------------------------------------------------------------ DELETE /api/transaction-locales/{id}

    @Test
    @WithLongPrincipal(1L)
    void delete_retorna204() throws Exception {
        doNothing().when(transactionLocaleService).delete(1L, 1L);

        mockMvc.perform(delete("/api/transaction-locales/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithLongPrincipal(1L)
    void delete_naoEncontrado_retorna404() throws Exception {
        doThrow(new ResourceNotFoundException("not found")).when(transactionLocaleService).delete(99L, 1L);

        mockMvc.perform(delete("/api/transaction-locales/99"))
                .andExpect(status().isNotFound());
    }
}
