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

    public UserResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("error.auth.invalidCredentials"));
        if (!passwordEncoder.matches(password, user.getPassword()))
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

        User user = new User(null, req.username(), req.email(), passwordEncoder.encode(req.password()));
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
