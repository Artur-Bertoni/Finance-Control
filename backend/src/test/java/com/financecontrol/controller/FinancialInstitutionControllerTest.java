package com.financecontrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.request.FinancialInstitutionRequest;
import com.financecontrol.dto.response.FinancialInstitutionResponse;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.service.FinancialInstitutionService;
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
@WebMvcTest(FinancialInstitutionController.class)
@AutoConfigureMockMvc(addFilters = false)
class FinancialInstitutionControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean FinancialInstitutionService                     financialInstitutionService;
    @MockitoBean JwtAuthFilter                                   jwtAuthFilter;
    @MockitoBean OAuth2UserService                               oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler              oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository      cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver        customAuthResolver;

    // ------------------------------------------------------------------ helpers

    private static FinancialInstitutionResponse fiResp(Long id, String name) {
        return new FinancialInstitutionResponse(id, name, null, null, null, LocalDateTime.now());
    }

    // ------------------------------------------------------------------ GET /api/financial-institutions

    @Test
    @WithLongPrincipal(1L)
    void findAll_retorna200ComLista() throws Exception {
        when(financialInstitutionService.findAllByUser(1L)).thenReturn(List.of(fiResp(1L, "Nubank")));

        mockMvc.perform(get("/api/financial-institutions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Nubank"));
    }

    // ------------------------------------------------------------------ GET /api/financial-institutions/{id}

    @Test
    @WithLongPrincipal(1L)
    void findById_encontrado_retorna200() throws Exception {
        when(financialInstitutionService.findById(1L)).thenReturn(fiResp(1L, "Bradesco"));

        mockMvc.perform(get("/api/financial-institutions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bradesco"));
    }

    @Test
    @WithLongPrincipal(1L)
    void findById_naoEncontrado_retorna404() throws Exception {
        when(financialInstitutionService.findById(99L)).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/financial-institutions/99"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ POST /api/financial-institutions

    @Test
    @WithLongPrincipal(1L)
    void create_requestValido_retorna200() throws Exception {
        FinancialInstitutionRequest req = new FinancialInstitutionRequest("Itaú", null, null, null);
        when(financialInstitutionService.create(eq(1L), any(), eq(false))).thenReturn(fiResp(2L, "Itaú"));

        mockMvc.perform(post("/api/financial-institutions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Itaú"));
    }

    // ------------------------------------------------------------------ PUT /api/financial-institutions/{id}

    @Test
    @WithLongPrincipal(1L)
    void update_requestValido_retorna200() throws Exception {
        FinancialInstitutionRequest req = new FinancialInstitutionRequest("Itaú Atualizado", null, null, null);
        when(financialInstitutionService.update(eq(1L), any())).thenReturn(fiResp(1L, "Itaú Atualizado"));

        mockMvc.perform(put("/api/financial-institutions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Itaú Atualizado"));
    }

    // ------------------------------------------------------------------ DELETE /api/financial-institutions/{id}

    @Test
    @WithLongPrincipal(1L)
    void delete_retorna204() throws Exception {
        doNothing().when(financialInstitutionService).delete(1L);

        mockMvc.perform(delete("/api/financial-institutions/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithLongPrincipal(1L)
    void delete_naoEncontrado_retorna404() throws Exception {
        doThrow(new ResourceNotFoundException("not found")).when(financialInstitutionService).delete(99L);

        mockMvc.perform(delete("/api/financial-institutions/99"))
                .andExpect(status().isNotFound());
    }
}
