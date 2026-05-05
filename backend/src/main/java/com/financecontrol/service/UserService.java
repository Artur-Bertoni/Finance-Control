package com.financecontrol.service;

import com.financecontrol.dto.request.UserRequest;
import com.financecontrol.dto.response.UserResponse;
import com.financecontrol.entity.User;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.exception.UnauthorizedException;
import com.financecontrol.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public UserResponse login(String email, String password) {
        User user = repository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new UnauthorizedException("Email e/ou senha incorreto(s)"));
        return UserResponse.from(user);
    }

    public UserResponse findById(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse create(UserRequest req) {
        if (repository.findByEmail(req.email()).isPresent())
            throw new BusinessException("Email já cadastrado");
        if (!req.password().equals(req.passwordConfirmation()))
            throw new BusinessException("As senhas devem ser iguais");

        User user = new User(null, req.username(), req.email(), req.password());
        return UserResponse.from(repository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest req) {
        if (repository.existsByEmailAndIdNot(req.email(), id))
            throw new BusinessException("Email já cadastrado");
        if (!req.password().equals(req.passwordConfirmation()))
            throw new BusinessException("As senhas devem ser iguais");

        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPassword(req.password());
        return UserResponse.from(repository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id))
            throw new ResourceNotFoundException("Usuário não encontrado");
        repository.deleteById(id);
    }
}
