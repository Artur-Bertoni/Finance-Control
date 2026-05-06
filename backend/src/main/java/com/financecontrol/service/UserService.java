package com.financecontrol.service;

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

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse login(String email, String password) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("error.auth.invalidCredentials"));
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new UnauthorizedException("error.auth.invalidCredentials");
        return UserResponse.from(user);
    }

    public UserResponse findById(@NonNull Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.user"));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse create(UserRequest req) {
        if (repository.findByEmail(req.email()).isPresent())
            throw new BusinessException("error.user.duplicateEmail");
        if (!req.password().equals(req.passwordConfirmation()))
            throw new BusinessException("error.user.passwordMismatch");

        User user = new User(null, req.username(), req.email(), passwordEncoder.encode(req.password()));
        return UserResponse.from(repository.save(user));
    }

    @Transactional
    public UserResponse update(@NonNull Long id, UserRequest req) {
        if (repository.existsByEmailAndIdNot(req.email(), id))
            throw new BusinessException("error.user.duplicateEmail");
        if (!req.password().equals(req.passwordConfirmation()))
            throw new BusinessException("error.user.passwordMismatch");

        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.user"));
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        return UserResponse.from(repository.save(user));
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!repository.existsById(id))
            throw new ResourceNotFoundException("error.notFound.user");
        repository.deleteById(id);
    }
}
