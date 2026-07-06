package com.financecontrol.controller;

import com.financecontrol.annotation.WithLongPrincipal;
import com.financecontrol.config.CookieOAuth2AuthorizationRequestRepository;
import com.financecontrol.config.CustomOAuth2AuthorizationRequestResolver;
import com.financecontrol.config.JwtAuthFilter;
import com.financecontrol.config.OAuth2AuthenticationSuccessHandler;
import com.financecontrol.entity.User;
import com.financecontrol.service.OAuth2UserService;
import com.financecontrol.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminTestController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminTestControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean UserService userService;

    @MockitoBean JwtAuthFilter                              jwtAuthFilter;
    @MockitoBean OAuth2UserService                          oauth2UserService;
    @MockitoBean OAuth2AuthenticationSuccessHandler         oauth2SuccessHandler;
    @MockitoBean CookieOAuth2AuthorizationRequestRepository cookieAuthRepo;
    @MockitoBean CustomOAuth2AuthorizationRequestResolver   customAuthResolver;

    @Test
    @WithLongPrincipal(1L)
    void markEmailUnverified_admin_retorna204() throws Exception {
        when(userService.findEntityById(1L)).thenReturn(adminUser(1L, true));

        mockMvc.perform(post("/api/admin/test/mark-email-unverified"))
                .andExpect(status().isNoContent());

        verify(userService).markEmailUnverified(1L);
    }

    @Test
    @WithLongPrincipal(1L)
    void markEmailUnverified_naoAdmin_retorna401() throws Exception {
        when(userService.findEntityById(1L)).thenReturn(adminUser(1L, false));

        mockMvc.perform(post("/api/admin/test/mark-email-unverified"))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).markEmailUnverified(any());
    }

    private static User adminUser(Long id, boolean admin) {
        User u = new User();
        u.setId(id);
        u.setUsername("admin");
        u.setEmail("admin@test.com");
        u.setAdmin(admin);
        return u;
    }
}
