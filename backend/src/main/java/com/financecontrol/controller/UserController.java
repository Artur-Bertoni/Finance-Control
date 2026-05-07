package com.financecontrol.controller;

import com.financecontrol.dto.request.PasswordChangeRequest;
import com.financecontrol.dto.request.UserRequest;
import com.financecontrol.dto.response.UserResponse;
import com.financecontrol.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest req) {
        return ResponseEntity.ok(userService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable @NonNull Long id,
                                               @RequestBody UserRequest req,
                                               HttpSession session) {
        requireUserId(session);
        UserResponse updated = userService.update(id, req);
        session.setAttribute("userId", updated.id());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable @NonNull Long id,
                                               @RequestBody PasswordChangeRequest req,
                                               HttpSession session) {
        requireUserId(session);
        userService.changePassword(id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id, HttpSession session) {
        requireUserId(session);
        userService.delete(id);
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
