package com.financecontrol.controller;

import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.entity.User;
import com.financecontrol.service.EmailService;
import com.financecontrol.service.OAuth2UserService;
import com.financecontrol.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminEmailController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminEmailControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean UserService  userService;
    @MockitoBean EmailService emailService;

    @MockitoBean JwtAuthFilter                              jwtAuthFilter;
    @MockitoBean OAuth2UserService                          oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler         oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver   customAuthResolver;

    @Test
    @WithLongPrincipal(1L)
    void sendTestEmail_adminWeekly_retorna200() throws Exception {
        User admin = adminUser(1L);
        when(userService.findEntityById(1L)).thenReturn(admin);

        mockMvc.perform(post("/api/admin/email/send-test").param("type", "WEEKLY"))
                .andExpect(status().isOk());

        verify(emailService).sendTestEmail(admin);
    }

    @Test
    @WithLongPrincipal(1L)
    void sendTestEmail_naoAdmin_retorna401() throws Exception {
        User naoAdmin = adminUser(1L);
        naoAdmin.setAdmin(false);
        when(userService.findEntityById(1L)).thenReturn(naoAdmin);

        mockMvc.perform(post("/api/admin/email/send-test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithLongPrincipal(1L)
    void sendTestEmail_tipoInvalido_retorna400() throws Exception {
        User admin = adminUser(1L);
        when(userService.findEntityById(1L)).thenReturn(admin);

        mockMvc.perform(post("/api/admin/email/send-test").param("type", "INVALID_TYPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithLongPrincipal(1L)
    void sendTestEmail_verification_retorna200() throws Exception {
        User admin = adminUser(1L);
        when(userService.findEntityById(1L)).thenReturn(admin);

        mockMvc.perform(post("/api/admin/email/send-test").param("type", "VERIFICATION"))
                .andExpect(status().isOk());

        verify(emailService).sendTestVerificationEmail(admin);
    }

    @Test
    @WithLongPrincipal(1L)
    void sendTestEmail_feedback_retorna200() throws Exception {
        User admin = adminUser(1L);
        when(userService.findEntityById(1L)).thenReturn(admin);

        mockMvc.perform(post("/api/admin/email/send-test").param("type", "FEEDBACK"))
                .andExpect(status().isOk());

        verify(emailService).sendTestFeedbackEmail(admin);
    }

    @Test
    @WithLongPrincipal(1L)
    void sendTestEmail_goalDeadline_retorna200() throws Exception {
        User admin = adminUser(1L);
        when(userService.findEntityById(1L)).thenReturn(admin);

        mockMvc.perform(post("/api/admin/email/send-test").param("type", "GOAL_DEADLINE_WARNING"))
                .andExpect(status().isOk());

        verify(emailService).sendTestGoalDeadlineEmail(admin);
        verify(emailService, never()).sendTestEmail(any());
    }

    @Test
    @WithLongPrincipal(1L)
    void sendTestEmail_goalEmailFalha_retorna400() throws Exception {
        User admin = adminUser(1L);
        when(userService.findEntityById(1L)).thenReturn(admin);
        doThrow(new RuntimeException("smtp down")).when(emailService).sendTestGoalDeadlineEmail(any());

        mockMvc.perform(post("/api/admin/email/send-test").param("type", "GOAL_DEADLINE_WARNING"))
                .andExpect(status().isBadRequest());
    }

    private static User adminUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("admin");
        u.setEmail("admin@test.com");
        u.setAdmin(true);
        return u;
    }
}
