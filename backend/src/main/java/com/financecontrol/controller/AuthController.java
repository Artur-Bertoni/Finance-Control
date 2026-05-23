package com.financecontrol.controller;

import com.financecontrol.config.JwtUtil;
import com.financecontrol.dto.request.LoginRequest;
import com.financecontrol.dto.response.UserResponse;
import com.financecontrol.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController extends BaseController {

    private final UserService userService;
    private final JwtUtil     jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest req, 
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        UserResponse user = userService.login(req.identifier(), req.password());
        boolean secure = request.isSecure();

        jwtUtil.setTokenCookie(response, jwtUtil.generateToken(user.id()), secure);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        jwtUtil.clearTokenCookie(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userService.findById(requireUserId()));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        Long userId = userService.verifyEmail(token);

        jwtUtil.setTokenCookie(response, jwtUtil.generateToken(userId), request.isSecure());
        response.setHeader("Location", "/pages/AppShell.html");

        return ResponseEntity.status(302).build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@RequestParam String email) {
        userService.resendVerification(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/link/google")
    public ResponseEntity<Map<String, String>> linkGoogle() {
        requireUserId();
        return ResponseEntity.ok(Map.of("redirectUrl", "/oauth2/authorization/google?link=true"));
    }

    @DeleteMapping("/link/google")
    public ResponseEntity<Void> unlinkGoogle() {
        userService.unlinkGoogle(requireUserId());
        return ResponseEntity.noContent().build();
    }
}
