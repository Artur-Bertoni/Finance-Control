package com.financecontrol.service;

import com.financecontrol.dto.request.UserFeedbackRequest;
import com.financecontrol.dto.response.UserFeedbackResponse;
import com.financecontrol.entity.User;
import com.financecontrol.entity.UserFeedback;
import com.financecontrol.enums.FeedbackType;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.UserFeedbackRepository;
import com.financecontrol.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class UserFeedbackServiceTest {

    @Mock UserFeedbackRepository feedbackRepository;
    @Mock UserRepository         userRepository;
    @Mock EmailService           emailService;

    @InjectMocks UserFeedbackService feedbackService;

    @Test
    void submit_sucesso_retornaResponse() {
        User user = userWith(1L, "joao", "joao@test.com", false);
        UserFeedbackRequest req = req("SUGGESTION", "Adicionem suporte a exportação em CSV", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findAllByAdminTrueAndActiveTrue()).thenReturn(List.of());
        when(feedbackRepository.save(any(UserFeedback.class))).thenAnswer(inv -> {
            UserFeedback f = inv.getArgument(0);
            f.setId(1L);
            f.setCreatedAt(LocalDateTime.now());
            return f;
        });

        UserFeedbackResponse resp = feedbackService.submit(1L, req);

        assertThat(resp.getType()).isEqualTo("SUGGESTION");
        assertThat(resp.getMessage()).isEqualTo("Adicionem suporte a exportação em CSV");
        assertThat(resp.getNpsScore()).isNull();
    }

    @Test
    void submit_comNps_persisteNpsScore() {
        User user = userWith(1L, "maria", "maria@test.com", false);
        UserFeedbackRequest req = req("BUG", "O botão não responde no mobile", 8);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findAllByAdminTrueAndActiveTrue()).thenReturn(List.of());
        when(feedbackRepository.save(any(UserFeedback.class))).thenAnswer(inv -> {
            UserFeedback f = inv.getArgument(0);
            f.setId(2L);
            f.setCreatedAt(LocalDateTime.now());
            return f;
        });

        UserFeedbackResponse resp = feedbackService.submit(1L, req);

        assertThat(resp.getNpsScore()).isEqualTo(8);
        assertThat(resp.getType()).isEqualTo("BUG");
    }

    @Test
    void submit_enviEmailParaAdmins() {
        User user  = userWith(1L, "user", "user@test.com", false);
        User admin = userWith(2L, "admin", "admin@test.com", true);
        UserFeedbackRequest req = req("GENERAL", "Ótima aplicação!", 10);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findAllByAdminTrueAndActiveTrue()).thenReturn(List.of(admin));
        when(feedbackRepository.save(any(UserFeedback.class))).thenAnswer(inv -> {
            UserFeedback f = inv.getArgument(0);
            f.setId(3L);
            f.setCreatedAt(LocalDateTime.now());
            return f;
        });

        feedbackService.submit(1L, req);

        verify(emailService).sendFeedbackNotification(eq(admin), eq(user), any(UserFeedback.class));
    }

    @Test
    void submit_usuarioNaoEncontrado_lancaResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        UserFeedbackRequest badReq = req("GENERAL", "teste mensagem aqui", null);

        assertThatThrownBy(() -> feedbackService.submit(99L, badReq))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_semFiltro_retornaPaginado() {
        UserFeedback f = feedbackWith(1L, userWith(1L, "u", "u@t.com", false),
                FeedbackType.SUGGESTION, "Texto de sugestão aqui", null);
        Page<UserFeedback> page = new PageImpl<>(List.of(f));

        when(feedbackRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(page);

        Page<UserFeedbackResponse> result = feedbackService.findAll(0, 20, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo("SUGGESTION");
    }

    @Test
    void findAll_comFiltroTipo_filtraPorTipo() {
        Page<UserFeedback> page = Page.empty();

        when(feedbackRepository.findByTypeOrderByCreatedAtDesc(eq(FeedbackType.BUG), any(Pageable.class)))
                .thenReturn(page);

        Page<UserFeedbackResponse> result = feedbackService.findAll(0, 20, "BUG");

        assertThat(result.getContent()).isEmpty();
        verify(feedbackRepository, never()).findAllByOrderByCreatedAtDesc(any());
    }

    private static User userWith(Long id, String username, String email, boolean admin) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(email);
        u.setAdmin(admin);
        return u;
    }

    private static UserFeedbackRequest req(String type, String message, Integer nps) {
        UserFeedbackRequest r = new UserFeedbackRequest();
        r.setType(type);
        r.setMessage(message);
        r.setNpsScore(nps);
        return r;
    }

    private static UserFeedback feedbackWith(Long id, User user, FeedbackType type,
                                              String message, Integer nps) {
        UserFeedback f = new UserFeedback();
        f.setId(id);
        f.setUser(user);
        f.setType(type);
        f.setMessage(message);
        f.setNpsScore(nps);
        f.setCreatedAt(LocalDateTime.now());
        return f;
    }
}
