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

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String NOT_FOUND = "error.notFound.user";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        user.setLanguage("pt");
        user.setAdmin(false);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(@NonNull Long id, UserRequest req) {
        if (userRepository.existsByEmailAndIdNot(req.email(), id))
            throw new BusinessException("error.user.duplicateEmail");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));
        user.setUsername(req.username());
        user.setEmail(req.email());
        if (req.emailNotificationEnabled() != null)
            user.setEmailNotificationEnabled(req.emailNotificationEnabled());
        if (req.emailNotificationDay() != null)
            user.setEmailNotificationDay(req.emailNotificationDay());
        if (req.language() != null)
            user.setLanguage(req.language());
        return UserResponse.from(userRepository.save(user));
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
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!userRepository.existsById(id))
            throw new ResourceNotFoundException(NOT_FOUND);
        userRepository.deleteById(id);
    }
}
