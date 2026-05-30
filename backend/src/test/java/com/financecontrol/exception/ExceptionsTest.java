package com.financecontrol.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ExceptionsTest {

    // ── BusinessException ────────────────────────────────────────────────────

    @Test
    void businessException_mensagem_guardada() {
        BusinessException ex = new BusinessException("error.duplicate.name");
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("error.duplicate.name");
    }

    // ── ResourceNotFoundException ────────────────────────────────────────────

    @Test
    void resourceNotFoundException_mensagem_guardada() {
        ResourceNotFoundException ex = new ResourceNotFoundException("error.notFound.account");
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("error.notFound.account");
    }

    // ── UnauthorizedException ────────────────────────────────────────────────

    @Test
    void unauthorizedException_mensagem_guardada() {
        UnauthorizedException ex = new UnauthorizedException("error.auth.invalidCredentials");
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("error.auth.invalidCredentials");
    }

    // ── hierarquia não interfere entre si ────────────────────────────────────

    @Test
    void excecoes_saoTiposDistintos() {
        assertThat(new BusinessException("x"))
                .isNotInstanceOf(ResourceNotFoundException.class)
                .isNotInstanceOf(UnauthorizedException.class);

        assertThat(new ResourceNotFoundException("x"))
                .isNotInstanceOf(BusinessException.class)
                .isNotInstanceOf(UnauthorizedException.class);

        assertThat(new UnauthorizedException("x"))
                .isNotInstanceOf(BusinessException.class)
                .isNotInstanceOf(ResourceNotFoundException.class);
    }
}
