package com.financecontrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.request.CategoryRequest;
import com.financecontrol.dto.response.CategoryResponse;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.service.CategoryService;
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
@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean CategoryService                                 categoryService;
    @MockitoBean JwtAuthFilter                                   jwtAuthFilter;
    @MockitoBean OAuth2UserService                               oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler              oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository      cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver        customAuthResolver;

    // ------------------------------------------------------------------ helpers

    private static CategoryResponse catResp(Long id, String name) {
        return new CategoryResponse(id, name, null, null, List.of(), LocalDateTime.now());
    }

    // ------------------------------------------------------------------ GET /api/categories

    @Test
    @WithLongPrincipal(1L)
    void findAll_retorna200ComLista() throws Exception {
        when(categoryService.findAllByUser(1L)).thenReturn(List.of(catResp(1L, "Alimentação")));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alimentação"));
    }

    // ------------------------------------------------------------------ GET /api/categories/{id}

    @Test
    @WithLongPrincipal(1L)
    void findById_encontrado_retorna200() throws Exception {
        when(categoryService.findById(1L, 1L)).thenReturn(catResp(1L, "Transporte"));

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Transporte"));
    }

    @Test
    @WithLongPrincipal(1L)
    void findById_naoEncontrado_retorna404() throws Exception {
        when(categoryService.findById(99L, 1L)).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ POST /api/categories

    @Test
    @WithLongPrincipal(1L)
    void create_requestValido_retorna200() throws Exception {
        CategoryRequest req = new CategoryRequest("Lazer", null, null, List.of());
        when(categoryService.create(eq(1L), any(), eq(false))).thenReturn(catResp(2L, "Lazer"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lazer"));
    }

    // ------------------------------------------------------------------ PUT /api/categories/{id}

    @Test
    @WithLongPrincipal(1L)
    void update_requestValido_retorna200() throws Exception {
        CategoryRequest req = new CategoryRequest("Lazer Atualizado", null, null, List.of());
        when(categoryService.update(eq(1L), eq(1L), any())).thenReturn(catResp(1L, "Lazer Atualizado"));

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lazer Atualizado"));
    }

    // ------------------------------------------------------------------ DELETE /api/categories/{id}

    @Test
    @WithLongPrincipal(1L)
    void delete_retorna204() throws Exception {
        doNothing().when(categoryService).delete(1L, 1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithLongPrincipal(1L)
    void delete_naoEncontrado_retorna404() throws Exception {
        doThrow(new ResourceNotFoundException("not found")).when(categoryService).delete(99L, 1L);

        mockMvc.perform(delete("/api/categories/99"))
                .andExpect(status().isNotFound());
    }
}
