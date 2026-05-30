package com.financecontrol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.dto.request.UserFeedbackRequest;
import com.financecontrol.dto.response.UserFeedbackResponse;
import com.financecontrol.entity.User;
import com.financecontrol.repository.UserRepository;
import com.financecontrol.service.OAuth2UserService;
import com.financecontrol.service.UserFeedbackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(UserFeedbackController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserFeedbackControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean UserFeedbackService                        feedbackService;
    @MockitoBean UserRepository                             userRepository;
    @MockitoBean JwtAuthFilter                              jwtAuthFilter;
    @MockitoBean OAuth2UserService                          oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler         oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver   customAuthResolver;

    @Test
    @WithLongPrincipal(1L)
    void submit_requestValido_retorna200() throws Exception {
        UserFeedbackRequest req  = req("SUGGESTION", "Adicionar exportação CSV", null);
        UserFeedbackResponse resp = resp(1L, "SUGGESTION", "Adicionar exportação CSV", null);

        when(feedbackService.submit(eq(1L), any())).thenReturn(resp);

        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SUGGESTION"));
    }

    @Test
    @WithLongPrincipal(1L)
    void adminFeedbacks_usuarioNaoAdmin_retorna401() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWith(1L, false)));

        mockMvc.perform(get("/api/admin/feedbacks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithLongPrincipal(2L)
    void adminFeedbacks_usuarioAdmin_retorna200() throws Exception {
        Page<UserFeedbackResponse> page = new PageImpl<>(List.of(
                resp(1L, "BUG", "Crash ao salvar meta", 7)
        ));

        when(userRepository.findById(2L)).thenReturn(Optional.of(userWith(2L, true)));
        when(feedbackService.findAll(anyInt(), anyInt(), any())).thenReturn(page);

        mockMvc.perform(get("/api/admin/feedbacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("BUG"));
    }

    private static UserFeedbackRequest req(String type, String message, Integer nps) {
        UserFeedbackRequest r = new UserFeedbackRequest();
        r.setType(type);
        r.setMessage(message);
        r.setNpsScore(nps);
        return r;
    }

    private static UserFeedbackResponse resp(Long id, String type, String message, Integer nps) {
        return new UserFeedbackResponse(id, "testuser", "test@test.com",
                type, message, nps, LocalDateTime.now());
    }

    private static User userWith(Long id, boolean admin) {
        User u = new User();
        u.setId(id);
        u.setUsername("user" + id);
        u.setEmail("user" + id + "@test.com");
        u.setAdmin(admin);
        return u;
    }
}
