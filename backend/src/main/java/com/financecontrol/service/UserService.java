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
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.financecontrol.service.HistoryService.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String NOT_FOUND = "error.notFound.user";

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final HistoryService historyService;
    private final EmailService emailService;

    public UserResponse login(String identifier,
                              String password) {
        User user = identifier.contains("@")
                ? userRepository.findByEmail(identifier).orElse(null)
                : userRepository.findByUsername(identifier).orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPassword()))
            throw new UnauthorizedException("error.auth.invalidCredentials");

        return UserResponse.from(user);
    }

    public UserResponse findById(@NonNull Long id) {
        return UserResponse.from(findEntityById(id));
    }

    public User findEntityById(@NonNull Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));
    }

    @Transactional
    public UserResponse create(UserRequest req) {
        if (userRepository.findByEmail(req.email()).isPresent())
            throw new BusinessException("error.user.duplicateEmail");
        if (!req.password().equals(req.passwordConfirmation()))
            throw new BusinessException("error.user.passwordMismatch");

        User user = new User();

        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setEmailNotificationEnabled(Boolean.TRUE.equals(req.emailNotificationEnabled()));
        user.setEmailNotificationDay(req.emailNotificationDay() != null ? req.emailNotificationDay() : 5);
        user.setGoalEmailNotificationEnabled(true);
        user.setLanguage("pt");
        user.setAdmin(false);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());

        UserResponse result = UserResponse.from(userRepository.save(user));
        historyService.recordCreation(ENTITY_USER, result.id(), result.id());

        sendVerificationEmail(user);
        return result;
    }

    @Transactional
    public Long verifyEmail(String token) {
        EmailVerificationToken evt = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("error.auth.invalidVerificationToken"));

        if (evt.isExpired())
            throw new BusinessException("error.auth.expiredVerificationToken");

        User user = userRepository.findById(Objects.requireNonNull(evt.getUserId()))
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));

        user.setEmailVerified(true);
        userRepository.save(user);
        emailVerificationTokenRepository.deleteByUserId(user.getId());
        return java.util.Objects.requireNonNull(user.getId());
    }

    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));
        if (user.isEmailVerified()) return;
        emailVerificationTokenRepository.deleteByUserId(user.getId());
        sendVerificationEmail(user);
    }

    @Transactional
    public void unlinkGoogle(@NonNull Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));
        if (user.getPassword() == null || user.getPassword().isBlank())
            throw new BusinessException("error.auth.cannotUnlinkNoPassword");
        user.setProvider(null);
        user.setProviderId(null);
        userRepository.save(user);
    }

    @Transactional
    public Long resolveOAuth2Login(String provider,
                                   String providerId,
                                   String email,
                                   String name) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    User user = email != null ? userRepository.findByEmail(email).orElse(null) : null;
                    if (user == null) {
                        user = new User();
                        user.setEmail(email);
                        user.setUsername(generateOAuth2Username(name, email));
                        user.setLanguage("pt");
                        user.setAdmin(false);
                        user.setEmailNotificationEnabled(false);
                        user.setEmailNotificationDay(5);
                        user.setGoalEmailNotificationEnabled(true);
                        user.setCreatedAt(LocalDateTime.now());
                    }
                    user.setProvider(provider);
                    user.setProviderId(providerId);
                    user.setEmailVerified(true);
                    return userRepository.save(user);
                }).getId();
    }

    @Transactional
    public void linkGoogleAccount(@NonNull Long userId,
                                  String provider,
                                  String providerId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));

        userRepository.findByProviderAndProviderId(provider, providerId)
            .ifPresent(existing -> {
                if (!existing.getId().equals(userId))
                    throw new BusinessException("error.auth.googleAlreadyLinked");
            });

        user.setProvider(provider);
        user.setProviderId(providerId);
        user.setEmailVerified(true);

        userRepository.save(user);
    }

    private String generateOAuth2Username(String name,
                                          String email) {
        String emailBase = email != null ? email.split("@")[0] : "user";
        String base = name != null && !name.isBlank()
                ? name.toLowerCase().replaceAll("[^a-z0-9]", "")
                : emailBase;

        if (base.isBlank()) base = "user";

        String candidate = base;
        int i = 1;

        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = base + i++;
        }

        return candidate;
    }

    @Transactional
    public UserResponse update(@NonNull Long id,
                               UserRequest req) {
        if (userRepository.existsByEmailAndIdNot(req.email(), id))
            throw new BusinessException("error.user.duplicateEmail");

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));

        Map<String, String[]> diff = new LinkedHashMap<>();

        if (differs(user.getUsername(), req.username()))
            diff.put("username", diff(user.getUsername(), req.username()));
        if (differs(user.getEmail(), req.email()))
            diff.put("email", diff(user.getEmail(), req.email()));
        if (req.emailNotificationEnabled() != null && differs(user.isEmailNotificationEnabled(), req.emailNotificationEnabled()))
            diff.put("emailNotificationEnabled", diff(String.valueOf(user.isEmailNotificationEnabled()), String.valueOf(req.emailNotificationEnabled())));
        if (req.emailNotificationDay() != null && differs(user.getEmailNotificationDay(), req.emailNotificationDay()))
            diff.put("emailNotificationDay", diff(String.valueOf(user.getEmailNotificationDay()), String.valueOf(req.emailNotificationDay())));
        if (req.goalEmailNotificationEnabled() != null && differs(user.isGoalEmailNotificationEnabled(), req.goalEmailNotificationEnabled()))
            diff.put("goalEmailNotificationEnabled", diff(String.valueOf(user.isGoalEmailNotificationEnabled()), String.valueOf(req.goalEmailNotificationEnabled())));
        if (req.language() != null && differs(user.getLanguage(), req.language()))
            diff.put("language", diff(user.getLanguage(), req.language()));

        user.setUsername(req.username());
        user.setEmail(req.email());

        if (req.emailNotificationEnabled() != null)
            user.setEmailNotificationEnabled(req.emailNotificationEnabled());
        if (req.emailNotificationDay() != null)
            user.setEmailNotificationDay(req.emailNotificationDay());
        if (req.goalEmailNotificationEnabled() != null)
            user.setGoalEmailNotificationEnabled(req.goalEmailNotificationEnabled());
        if (req.language() != null)
            user.setLanguage(req.language());

        UserResponse result = UserResponse.from(userRepository.save(user));
        historyService.recordChanges(ENTITY_USER, id, id, diff);

        return result;
    }

    @Transactional
    public void changePassword(@NonNull Long id,
                               PasswordChangeRequest req) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));

        if (user.getPassword() != null && !passwordEncoder.matches(req.currentPassword(), user.getPassword()))
            throw new BusinessException("error.user.wrongCurrentPassword");
        if (!req.newPassword().equals(req.passwordConfirmation()))
            throw new BusinessException("error.user.passwordMismatch");

        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        historyService.recordPasswordChange(id);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!userRepository.existsById(id))
            throw new ResourceNotFoundException(NOT_FOUND);

        userRepository.deleteById(id);
    }

    private void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString().replace("-", "");
         
        EmailVerificationToken evt = new EmailVerificationToken(
                null, user.getId(), token,
                LocalDateTime.now(), LocalDateTime.now().plusHours(24)
        );

        emailVerificationTokenRepository.save(evt);
        emailService.sendVerificationEmail(user, token);
    }
}
