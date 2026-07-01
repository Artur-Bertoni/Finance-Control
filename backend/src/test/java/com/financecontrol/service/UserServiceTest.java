package com.financecontrol.service;

import com.financecontrol.dto.request.PasswordChangeRequest;
import com.financecontrol.dto.request.UserRequest;
import com.financecontrol.dto.response.UserResponse;
import com.financecontrol.entity.EmailVerificationToken;
import com.financecontrol.entity.User;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.exception.UnauthorizedException;
import com.financecontrol.repository.EmailVerificationTokenRepository;
import com.financecontrol.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"null", "unchecked"})
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock HistoryService historyService;
    @Mock EmailService emailService;
    @Mock OnboardingService onboardingService;

    @InjectMocks UserService userService;

    // ── login ────────────────────────────────────────────────────────────────

    @Test
    void login_porEmail_sucesso() {
        User user = userWith(1L, "joao", "joao@test.com", "hash");
        when(userRepository.findByEmailAndActiveTrue("joao@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "hash")).thenReturn(true);

        UserResponse result = userService.login("joao@test.com", "senha123");

        assertThat(result.email()).isEqualTo("joao@test.com");
    }

    @Test
    void login_porUsername_sucesso() {
        User user = userWith(1L, "joao", "joao@test.com", "hash");
        when(userRepository.findByUsernameAndActiveTrue("joao")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "hash")).thenReturn(true);

        UserResponse result = userService.login("joao", "senha123");

        assertThat(result.username()).isEqualTo("joao");
    }

    @Test
    void login_usuarioNaoEncontrado_lancaUnauthorizedException() {
        when(userRepository.findByEmailAndActiveTrue("x@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login("x@x.com", "pass"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_senhaErrada_lancaUnauthorizedException() {
        User user = userWith(1L, "joao", "joao@test.com", "hash");
        when(userRepository.findByEmailAndActiveTrue("joao@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThatThrownBy(() -> userService.login("joao@test.com", "errada"))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_encontrado_retornaResponse() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(userWith(1L, "joao", "joao@test.com", "hash")));

        UserResponse result = userService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_naoEncontrado_lancaResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── findEntityById ───────────────────────────────────────────────────────

    @Test
    void findEntityById_naoEncontrado_lancaResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findEntityById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_sucesso_salvaUsuarioEEnviaEmail() {
        UserRequest req = new UserRequest("joao", "joao@test.com", "SenhaForte1!x", "SenhaForte1!x",
                true, 3, false, "pt");

        when(userRepository.findByEmailAndActiveTrue("joao@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("SenhaForte1!x")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.create(req);

        assertThat(result.username()).isEqualTo("joao");
        assertThat(result.email()).isEqualTo("joao@test.com");
        verify(emailService).sendVerificationEmail(any(User.class), anyString());
        verify(historyService).recordCreation(eq(HistoryService.ENTITY_USER), eq(10L), eq(10L));
    }

    @Test
    void create_emailDuplicado_lancaBusinessException() {
        UserRequest req = new UserRequest("joao", "joao@test.com", "abc123", "abc123",
                null, null, null, null);
        when(userRepository.findByEmailAndActiveTrue("joao@test.com"))
                .thenReturn(Optional.of(userWith(1L, "joao", "joao@test.com", "hash")));

        assertThatThrownBy(() -> userService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("duplicateEmail");
    }

    @Test
    void create_senhaFraca_lancaBusinessException() {
        UserRequest req = new UserRequest("joao", "joao@test.com", "fraca", "fraca",
                null, null, null, null);
        when(userRepository.findByEmailAndActiveTrue("joao@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("weakPassword");
    }

    @Test
    void create_senhasDivergentes_lancaBusinessException() {
        UserRequest req = new UserRequest("joao", "joao@test.com", "abc123", "diferente",
                null, null, null, null);
        when(userRepository.findByEmailAndActiveTrue("joao@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("passwordMismatch");
    }

    // ── verifyEmail ──────────────────────────────────────────────────────────

    @Test
    void verifyEmail_tokenValido_marcaEmailComoVerificado() {
        EmailVerificationToken evt = tokenWith(1L, 1L, "tk123", false);
        User user = userWith(1L, "joao", "joao@test.com", "hash");

        when(emailVerificationTokenRepository.findByToken("tk123")).thenReturn(Optional.of(evt));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Long userId = userService.verifyEmail("tk123");

        assertThat(userId).isEqualTo(1L);
        assertThat(user.isEmailVerified()).isTrue();
        verify(emailVerificationTokenRepository).deleteByUserId(1L);
    }

    @Test
    void verifyEmail_tokenInvalido_lancaBusinessException() {
        when(emailVerificationTokenRepository.findByToken("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.verifyEmail("bad"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("invalidVerificationToken");
    }

    @Test
    void verifyEmail_tokenExpirado_lancaBusinessException() {
        EmailVerificationToken evt = tokenWith(1L, 1L, "tk123", true);
        when(emailVerificationTokenRepository.findByToken("tk123")).thenReturn(Optional.of(evt));

        assertThatThrownBy(() -> userService.verifyEmail("tk123"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expiredVerificationToken");
    }

    // ── resendVerification ───────────────────────────────────────────────────

    @Test
    void resendVerification_emailNaoVerificado_enviaNovamenteEToken() {
        User user = userWith(1L, "joao", "joao@test.com", "hash");
        user.setEmailVerified(false);

        when(userRepository.findByEmailAndActiveTrue("joao@test.com")).thenReturn(Optional.of(user));
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        userService.resendVerification("joao@test.com");

        verify(emailVerificationTokenRepository).deleteByUserId(1L);
        verify(emailService).sendVerificationEmail(any(User.class), anyString());
    }

    @Test
    void resendVerification_emailJaVerificado_naoEnviaEmail() {
        User user = userWith(1L, "joao", "joao@test.com", "hash");
        user.setEmailVerified(true);
        when(userRepository.findByEmailAndActiveTrue("joao@test.com")).thenReturn(Optional.of(user));

        userService.resendVerification("joao@test.com");

        verify(emailService, never()).sendVerificationEmail(any(), any());
    }

    @Test
    void resendVerification_emailNaoEncontrado_naoEnviaEmailNemLancaExcecao() {
        when(userRepository.findByEmailAndActiveTrue("x@x.com")).thenReturn(Optional.empty());

        userService.resendVerification("x@x.com");

        verify(emailService, never()).sendVerificationEmail(any(), any());
    }

    // ── unlinkGoogle ─────────────────────────────────────────────────────────

    @Test
    void unlinkGoogle_sucesso_removeDadosProvider() {
        User user = userWith(1L, "joao", "joao@test.com", "hash");
        user.setProvider("google");
        user.setProviderId("gid");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.unlinkGoogle(1L);

        assertThat(user.getProvider()).isNull();
        assertThat(user.getProviderId()).isNull();
    }

    @Test
    void unlinkGoogle_semSenha_lancaBusinessException() {
        User user = userWith(1L, "joao", "joao@test.com", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.unlinkGoogle(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannotUnlinkNoPassword");
    }

    @Test
    void unlinkGoogle_naoEncontrado_lancaResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.unlinkGoogle(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── resolveOAuth2Login ───────────────────────────────────────────────────

    @Test
    void resolveOAuth2Login_existePorProvider_retornaIdExistente() {
        User user = userWith(1L, "joao", "joao@test.com", null);
        when(userRepository.findByProviderAndProviderIdAndActiveTrue("google", "gid123"))
                .thenReturn(Optional.of(user));

        Long id = userService.resolveOAuth2Login("google", "gid123", "joao@test.com", "João");

        assertThat(id).isEqualTo(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resolveOAuth2Login_existePorEmail_vinculaProvider() {
        User user = userWith(2L, "maria", "maria@test.com", null);
        when(userRepository.findByProviderAndProviderIdAndActiveTrue("google", "gid456"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActiveTrue("maria@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Long id = userService.resolveOAuth2Login("google", "gid456", "maria@test.com", "Maria");

        assertThat(id).isEqualTo(2L);
        assertThat(user.getProvider()).isEqualTo("google");
        assertThat(user.isEmailVerified()).isTrue();
    }

    @Test
    void resolveOAuth2Login_naoExiste_criaNovoUsuario() {
        when(userRepository.findByProviderAndProviderIdAndActiveTrue("google", "gid789"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActiveTrue("novo@test.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsernameAndActiveTrue(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });

        Long id = userService.resolveOAuth2Login("google", "gid789", "novo@test.com", "Novo");

        assertThat(id).isEqualTo(99L);
        verify(userRepository).save(any(User.class));
    }

    // ── linkGoogleAccount ────────────────────────────────────────────────────

    @Test
    void linkGoogleAccount_sucesso_vinculaProvider() {
        User user = userWith(1L, "joao", "joao@test.com", "hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByProviderAndProviderIdAndActiveTrue("google", "gid")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.linkGoogleAccount(1L, "google", "gid");

        assertThat(user.getProvider()).isEqualTo("google");
        assertThat(user.getProviderId()).isEqualTo("gid");
        assertThat(user.isEmailVerified()).isTrue();
    }

    @Test
    void linkGoogleAccount_googleJaVinculadoAOutro_lancaBusinessException() {
        User joao = userWith(1L, "joao", "joao@test.com", "hash");
        User outro = userWith(2L, "outro", "outro@test.com", null);
        outro.setProvider("google");
        outro.setProviderId("gid");

        when(userRepository.findById(1L)).thenReturn(Optional.of(joao));
        when(userRepository.findByProviderAndProviderIdAndActiveTrue("google", "gid")).thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> userService.linkGoogleAccount(1L, "google", "gid"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("googleAlreadyLinked");
    }

    @Test
    void linkGoogleAccount_naoEncontrado_lancaResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.linkGoogleAccount(99L, "google", "gid"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_sucesso_alteraDadosERegistraDiff() {
        User user = userWith(1L, "antigo", "antigo@test.com", "hash");
        when(userRepository.existsByEmailAndActiveTrueAndIdNot("novo@test.com", 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserRequest req = new UserRequest("novo", "novo@test.com", null, null, null, null, null, "en");
        UserResponse result = userService.update(1L, req);

        assertThat(result.username()).isEqualTo("novo");
        assertThat(result.email()).isEqualTo("novo@test.com");
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_USER), eq(1L), eq(1L), any());
    }

    @Test
    void update_emailDuplicado_lancaBusinessException() {
        when(userRepository.existsByEmailAndActiveTrueAndIdNot("dup@test.com", 1L)).thenReturn(true);
        UserRequest req = new UserRequest("x", "dup@test.com", null, null, null, null, null, null);

        assertThatThrownBy(() -> userService.update(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("duplicateEmail");
    }

    @Test
    void update_naoEncontrado_lancaResourceNotFoundException() {
        when(userRepository.existsByEmailAndActiveTrueAndIdNot("x@x.com", 99L)).thenReturn(false);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserRequest req = new UserRequest("x", "x@x.com", null, null, null, null, null, null);
        assertThatThrownBy(() -> userService.update(99L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── changePassword ───────────────────────────────────────────────────────

    @Test
    void changePassword_sucesso_encriptaESalva() {
        User user = userWith(1L, "joao", "joao@test.com", "oldHash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "oldHash")).thenReturn(true);
        when(passwordEncoder.encode("NovaSenha1!x")).thenReturn("newHash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.changePassword(1L, new PasswordChangeRequest("oldPass", "NovaSenha1!x", "NovaSenha1!x"));

        assertThat(user.getPassword()).isEqualTo("newHash");
        verify(historyService).recordPasswordChange(1L);
    }

    @Test
    void changePassword_senhaAtualErrada_lancaBusinessException() {
        User user = userWith(1L, "joao", "joao@test.com", "hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L,
                new PasswordChangeRequest("errada", "nova", "nova")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("wrongCurrentPassword");
    }

    @Test
    void changePassword_novasSenhasDivergentes_lancaBusinessException() {
        User user = userWith(1L, "joao", "joao@test.com", "hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("hash", "hash")).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword(1L,
                new PasswordChangeRequest("hash", "nova1", "nova2")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("passwordMismatch");
    }

    @Test
    void changePassword_semSenhaAtual_permiteAlterarSemVerificar() {
        User user = userWith(1L, "joao", "joao@test.com", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NovaSenha1!x")).thenReturn("novaHash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.changePassword(1L, new PasswordChangeRequest(null, "NovaSenha1!x", "NovaSenha1!x"));

        assertThat(user.getPassword()).isEqualTo("novaHash");
    }

    @Test
    void changePassword_naoEncontrado_lancaResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.changePassword(99L,
                new PasswordChangeRequest("a", "b", "b")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_encontrado_marcaComoInativoSemRemoverFisicamente() {
        User user = userWith(1L, "joao", "joao@test.com", "hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.delete(1L);

        assertThat(user.isActive()).isFalse();
        verify(userRepository).save(user);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void delete_jaInativo_naoFazNada() {
        User user = userWith(1L, "joao", "joao@test.com", "hash");
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_naoEncontrado_lancaResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── new coverage tests ───────────────────────────────────────────────────

    @Test
    void resolveOAuth2Login_novoUsuarioComUsernameColidindo_geraSufixo() {
        when(userRepository.findByProviderAndProviderIdAndActiveTrue("google", "gidX"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndActiveTrue("colide@test.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsernameAndActiveTrue("joao")).thenReturn(true);
        when(userRepository.existsByUsernameAndActiveTrue("joao1")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(50L);
            return u;
        });

        Long id = userService.resolveOAuth2Login("google", "gidX", "colide@test.com", "Joao");

        assertThat(id).isEqualTo(50L);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("joao1");
        assertThat(captor.getValue().isEmailVerified()).isTrue();
    }

    @Test
    void resolveOAuth2Login_emailNulo_geraUsernamePadrao() {
        when(userRepository.findByProviderAndProviderIdAndActiveTrue("google", "gidNull"))
                .thenReturn(Optional.empty());
        when(userRepository.existsByUsernameAndActiveTrue(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(60L);
            return u;
        });

        Long id = userService.resolveOAuth2Login("google", "gidNull", null, null);

        assertThat(id).isEqualTo(60L);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("user");
        assertThat(captor.getValue().getEmail()).isNull();
    }

    @Test
    void update_apenasFlagsNotificacao_atualizaCamposOpcionais() {
        User user = userWith(1L, "joao", "joao@test.com", "hash");
        user.setEmailNotificationEnabled(false);
        user.setGoalEmailNotificationEnabled(false);

        when(userRepository.existsByEmailAndActiveTrueAndIdNot("joao@test.com", 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserRequest req = new UserRequest("joao", "joao@test.com", null, null, true, 12, true, "pt");
        userService.update(1L, req);

        assertThat(user.isEmailNotificationEnabled()).isTrue();
        assertThat(user.getEmailNotificationDay()).isEqualTo(12);
        assertThat(user.isGoalEmailNotificationEnabled()).isTrue();

        ArgumentCaptor<Map<String, String[]>> diffCaptor = ArgumentCaptor.forClass(Map.class);
        verify(historyService).recordChanges(eq(HistoryService.ENTITY_USER), eq(1L), eq(1L), diffCaptor.capture());
        assertThat(diffCaptor.getValue())
                .containsKeys("emailNotificationEnabled", "emailNotificationDay", "goalEmailNotificationEnabled");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static User userWith(Long id, String username, String email, String password) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(password);
        u.setCreatedAt(LocalDateTime.now());
        u.setEmailNotificationDay(5);
        u.setLanguage("pt");
        return u;
    }

    private static EmailVerificationToken tokenWith(Long id, Long userId, String token, boolean expired) {
        LocalDateTime base = LocalDateTime.now();
        return new EmailVerificationToken(id, userId, token,
                base.minusHours(2),
                expired ? base.minusHours(1) : base.plusHours(23));
    }
}
