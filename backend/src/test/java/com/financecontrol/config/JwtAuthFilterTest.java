package com.financecontrol.config;

import com.financecontrol.repository.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock JwtUtil jwtUtil;
    @Mock UserRepository userRepository;
    @InjectMocks JwtAuthFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_tokenValido_defineAutenticacao() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtil.extractTokenFromRequest(req)).thenReturn("tok");
        when(jwtUtil.isValid("tok")).thenReturn(true);
        when(jwtUtil.extractUserId("tok")).thenReturn(42L);
        when(userRepository.existsByIdAndActiveTrue(42L)).thenReturn(true);

        filter.doFilterInternal(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(42L);
        verify(chain).doFilter(req, res);
    }

    @Test
    void doFilterInternal_usuarioInativo_naoAutentica() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtil.extractTokenFromRequest(req)).thenReturn("tok");
        when(jwtUtil.isValid("tok")).thenReturn(true);
        when(jwtUtil.extractUserId("tok")).thenReturn(42L);
        when(userRepository.existsByIdAndActiveTrue(42L)).thenReturn(false);

        filter.doFilterInternal(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, res);
    }

    @Test
    void doFilterInternal_semToken_naoAutentica() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtil.extractTokenFromRequest(req)).thenReturn(null);

        filter.doFilterInternal(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, res);
    }

    @Test
    void doFilterInternal_tokenInvalido_naoAutentica() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtil.extractTokenFromRequest(req)).thenReturn("bad");
        when(jwtUtil.isValid("bad")).thenReturn(false);

        filter.doFilterInternal(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, res);
    }
}
