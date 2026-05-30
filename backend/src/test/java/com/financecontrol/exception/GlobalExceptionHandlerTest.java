package com.financecontrol.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalExceptionHandlerTest {

    @Mock MessageSource messageSource;

    @InjectMocks GlobalExceptionHandler handler;

    @Test
    void handleNotFound_retorna404ComMensagemTraduzida() {
        when(messageSource.getMessage(eq("error.notFound.account"), any(), any(Locale.class)))
                .thenReturn("Conta nao encontrada");

        ResponseEntity<Map<String, String>> resp =
                handler.handleNotFound(new ResourceNotFoundException("error.notFound.account"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).containsEntry("message", "Conta nao encontrada");
        assertThat(resp.getBody()).containsEntry("errorCode", "error.notFound.account");
    }

    @Test
    void handleBusiness_retorna400() {
        when(messageSource.getMessage(eq("error.duplicate.name"), any(), any(Locale.class)))
                .thenReturn("Nome duplicado");

        ResponseEntity<Map<String, String>> resp =
                handler.handleBusiness(new BusinessException("error.duplicate.name"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).containsEntry("errorCode", "error.duplicate.name");
    }

    @Test
    void handleUnauthorized_retorna401() {
        when(messageSource.getMessage(eq("error.unauthorized"), any(), any(Locale.class)))
                .thenReturn("Nao autorizado");

        ResponseEntity<Map<String, String>> resp =
                handler.handleUnauthorized(new UnauthorizedException("error.unauthorized"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody()).containsEntry("message", "Nao autorizado");
    }

    @Test
    void handleNoResource_retorna404ComChaveInternaPadrao() {
        when(messageSource.getMessage(eq("error.internal"), any(), any(Locale.class)))
                .thenReturn("Erro interno");

        ResponseEntity<Map<String, String>> resp = handler.handleNoResource();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).containsEntry("errorCode", "error.internal");
    }

    @Test
    void handleGeneral_retorna500() {
        when(messageSource.getMessage(eq("error.internal"), any(), any(Locale.class)))
                .thenReturn("Erro interno");

        ResponseEntity<Map<String, String>> resp =
                handler.handleGeneral(new RuntimeException("boom"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody()).containsEntry("message", "Erro interno");
        assertThat(resp.getBody()).containsEntry("errorCode", "error.internal");
    }

    @Test
    void handleNotFound_msgKeyNulo_usaErrorInternal() {
        when(messageSource.getMessage(eq("error.internal"), any(), any(Locale.class)))
                .thenReturn("Erro interno");

        ResponseEntity<Map<String, String>> resp =
                handler.handleNotFound(new ResourceNotFoundException(null));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).containsEntry("errorCode", "error.internal");
    }

    @Test
    void handleBusiness_mensagemNaoTraduzida_usaChaveComoFallback() {
        when(messageSource.getMessage(eq("chave.inexistente"), any(), any(Locale.class)))
                .thenThrow(new NoSuchMessageException("chave.inexistente"));

        ResponseEntity<Map<String, String>> resp =
                handler.handleBusiness(new BusinessException("chave.inexistente"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).containsEntry("message", "chave.inexistente");
        assertThat(resp.getBody()).containsEntry("errorCode", "chave.inexistente");
    }
}
