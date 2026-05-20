package com.financecontrol.service;

import com.financecontrol.dto.request.PasswordChangeRequest;
import com.financecontrol.dto.request.UserRequest;
import com.financecontrol.dto.response.UserResponse;
import com.financecontrol.entity.User;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.exception.UnauthorizedException;
import com.financecontrol.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.financecontrol.service.ChangeHistoryService.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String NOT_FOUND = "error.notFound.user";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChangeHistoryService changeHistoryService;

    public UserResponse login(String identifier, String password) {
        User user = identifier.contains("@")
                ? userRepository.findByEmail(identifier).orElse(null)
                : userRepository.findByUsername(identifier).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword()))
            throw new UnauthorizedException("error.auth.invalidCredentials");
        return UserResponse.from(user);
    }

    public UserResponse findById(@NonNull Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));
        return UserResponse.from(user);
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
        user.setCreatedAt(LocalDateTime.now());
        UserResponse result = UserResponse.from(userRepository.save(user));
        changeHistoryService.recordCreation(ENTITY_USER, result.id(), result.id());
        return result;
    }

    @Transactional
    public UserResponse update(@NonNull Long id, UserRequest req) {
        if (userRepository.existsByEmailAndIdNot(req.email(), id))
            throw new BusinessException("error.user.duplicateEmail");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));

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
        changeHistoryService.recordChanges(ENTITY_USER, id, id, diff);
        return result;
    }

    @Transactional
    public void changePassword(@NonNull Long id, PasswordChangeRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));
        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword()))
            throw new BusinessException("error.user.wrongCurrentPassword");
        if (!req.newPassword().equals(req.passwordConfirmation()))
            throw new BusinessException("error.user.passwordMismatch");
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        changeHistoryService.recordPasswordChange(id);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!userRepository.existsById(id))
            throw new ResourceNotFoundException(NOT_FOUND);
        userRepository.deleteById(id);
    }
}
