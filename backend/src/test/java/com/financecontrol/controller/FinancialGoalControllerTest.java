package com.financecontrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.request.GoalRequest;
import com.financecontrol.dto.response.GoalResponse;
import com.financecontrol.enums.GoalStatus;
import com.financecontrol.enums.GoalType;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.service.GoalService;
import com.financecontrol.service.OAuth2UserService;
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
@WebMvcTest(FinancialGoalController.class)
@AutoConfigureMockMvc(addFilters = false)
class FinancialGoalControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean GoalService                                     goalService;
    @MockitoBean JwtAuthFilter                                   jwtAuthFilter;
    @MockitoBean OAuth2UserService                               oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler              oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository      cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver        customAuthResolver;

    // ------------------------------------------------------------------ helpers

    private static GoalResponse goalResp(Long id, String name) {
        return new GoalResponse(id, name, null, GoalType.SAVINGS, GoalStatus.ACTIVE,
                1000.0, LocalDate.now(), LocalDate.now().plusMonths(6),
                List.of(), List.of(),
                false, false, false, true, false, false,
                200.0, 20.0, LocalDateTime.now());
    }

    private static GoalRequest goalReq() {
        return new GoalRequest("Meta Viagem", null, GoalType.SAVINGS, 1000.0,
                LocalDate.now(), LocalDate.now().plusMonths(6),
                List.of(), List.of(),
                false, false, false, true, false, false);
    }

    // ------------------------------------------------------------------ GET /api/goals

    @Test
    @WithLongPrincipal(1L)
    void findAll_retorna200ComLista() throws Exception {
        when(goalService.findAllByUser(1L)).thenReturn(List.of(goalResp(1L, "Meta Viagem")));

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Meta Viagem"));
    }

    // ------------------------------------------------------------------ GET /api/goals/{id}

    @Test
    @WithLongPrincipal(1L)
    void findById_encontrado_retorna200() throws Exception {
        when(goalService.findById(1L)).thenReturn(goalResp(1L, "Meta Casa"));

        mockMvc.perform(get("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Meta Casa"));
    }

    @Test
    @WithLongPrincipal(1L)
    void findById_naoEncontrado_retorna404() throws Exception {
        when(goalService.findById(99L)).thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/goals/99"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ POST /api/goals

    @Test
    @WithLongPrincipal(1L)
    void create_requestValido_retorna200() throws Exception {
        when(goalService.create(eq(1L), any(), eq(false))).thenReturn(goalResp(2L, "Meta Viagem"));

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Meta Viagem"));
    }

    // ------------------------------------------------------------------ PUT /api/goals/{id}

    @Test
    @WithLongPrincipal(1L)
    void update_requestValido_retorna200() throws Exception {
        when(goalService.update(eq(1L), eq(1L), any())).thenReturn(goalResp(1L, "Meta Viagem Atualizada"));

        mockMvc.perform(put("/api/goals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Meta Viagem Atualizada"));
    }

    // ------------------------------------------------------------------ PUT /api/goals/{id}/archive

    @Test
    @WithLongPrincipal(1L)
    void archive_retorna204() throws Exception {
        doNothing().when(goalService).archive(1L);

        mockMvc.perform(put("/api/goals/1/archive"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithLongPrincipal(1L)
    void archive_naoEncontrado_retorna404() throws Exception {
        doThrow(new ResourceNotFoundException("not found")).when(goalService).archive(99L);

        mockMvc.perform(put("/api/goals/99/archive"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ DELETE /api/goals/{id}

    @Test
    @WithLongPrincipal(1L)
    void delete_retorna204() throws Exception {
        doNothing().when(goalService).delete(1L);

        mockMvc.perform(delete("/api/goals/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithLongPrincipal(1L)
    void delete_naoEncontrado_retorna404() throws Exception {
        doThrow(new ResourceNotFoundException("not found")).when(goalService).delete(99L);

        mockMvc.perform(delete("/api/goals/99"))
                .andExpect(status().isNotFound());
    }
}
