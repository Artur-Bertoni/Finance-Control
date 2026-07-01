package com.financecontrol.controller;

import com.financecontrol.config.JwtUtil;
import com.financecontrol.dto.request.PasswordChangeRequest;
import com.financecontrol.dto.request.UserRequest;
import com.financecontrol.dto.response.UserResponse;
import com.financecontrol.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController extends BaseController {

    private final UserService userService;
    private final JwtUtil     jwtUtil;

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest req) {
        return ResponseEntity.ok(userService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable @NonNull Long id,
                                               @RequestBody UserRequest req,
                                               HttpSession session) {
        requireSelf(id);
        return ResponseEntity.ok(userService.update(id, req));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable @NonNull Long id,
                                               @RequestBody PasswordChangeRequest req,
                                               HttpSession session) {
        requireSelf(id);
        userService.changePassword(id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id,
                                       HttpServletResponse response,
                                       HttpSession session) {
        requireSelf(id);
        userService.delete(id);
        jwtUtil.clearTokenCookie(response);
        return ResponseEntity.noContent().build();
    }
}
