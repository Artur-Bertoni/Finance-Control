package com.financecontrol.controller;

import tools.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.response.BulkDeletePreviewResponse;
import com.financecontrol.dto.response.BulkDeleteResponse;
import com.financecontrol.enums.BulkEntityType;
import com.financecontrol.service.BulkDeleteService;
import com.financecontrol.service.OAuth2UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BulkDeleteController.class)
@AutoConfigureMockMvc(addFilters = false)
class BulkDeleteControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean BulkDeleteService                          bulkDeleteService;
    @MockitoBean JwtAuthFilter                              jwtAuthFilter;
    @MockitoBean OAuth2UserService                          oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler         oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver   customAuthResolver;

    @Test
    @WithLongPrincipal(1L)
    void preview_retorna200ComImpacto() throws Exception {
        when(bulkDeleteService.preview(eq(BulkEntityType.CATEGORIES), anyList(), eq(1L)))
                .thenReturn(new BulkDeletePreviewResponse(2, 15, 0, 0));

        mockMvc.perform(post("/api/bulk-delete/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("type", "categories", "ids", List.of(1, 2)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").value(2))
                .andExpect(jsonPath("$.deletedTransactions").value(15));
    }

    @Test
    @WithLongPrincipal(1L)
    void delete_retorna200ComTotais() throws Exception {
        when(bulkDeleteService.delete(eq(BulkEntityType.TRANSACTIONS), anyList(), eq(1L)))
                .thenReturn(new BulkDeleteResponse(3, 3, 0));

        mockMvc.perform(post("/api/bulk-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("type", "transactions", "ids", List.of(1, 2, 3)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(3))
                .andExpect(jsonPath("$.skipped").value(0));
    }

    @Test
    @WithLongPrincipal(1L)
    void delete_semTipoRetorna400() throws Exception {
        mockMvc.perform(post("/api/bulk-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[1]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bulkDeleteService);
    }
}
