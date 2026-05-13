package com.financecontrol.controller;

import com.financecontrol.dto.request.LoginRequest;
import com.financecontrol.dto.response.UserResponse;
import com.financecontrol.exception.UnauthorizedException;
import com.financecontrol.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest req, HttpSession session) {
        UserResponse user = userService.login(req.identifier(), req.password());
        session.setAttribute("userId", user.id());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) throw new UnauthorizedException("Não autenticado");
        return ResponseEntity.ok(userService.findById(userId));
    }
}
