package com.financecontrol.service;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BrevoMailClient {

    private final RestClient restClient;
    private final String apiKey;

    public BrevoMailClient(@Value("${app.mail.brevo.api-key:}") String apiKey) {
        this.apiKey = apiKey;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .requestFactory(factory)
                .build();
    }

    public void send(String fromEmail,
                     String fromName,
                     String to,
                     String subject,
                     String html) throws MessagingException {
        if (apiKey == null || apiKey.isBlank())
            throw new MessagingException("BREVO_API_KEY não configurada");

        Map<String, Object> payload = Map.of(
                "sender",      Map.of("email", fromEmail, "name", fromName),
                "to",          List.of(Map.of("email", to)),
                "subject",     subject,
                "htmlContent", html
        );

        try {
            restClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new MessagingException(
                    "Brevo respondeu " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new MessagingException("Falha ao enviar e-mail via Brevo: " + e.getMessage(), e);
        }
    }
}
