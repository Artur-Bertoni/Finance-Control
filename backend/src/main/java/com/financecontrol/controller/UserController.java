package com.financecontrol.controller;

import com.financecontrol.dto.request.UserRequest;
import com.financecontrol.dto.response.UserResponse;
import com.financecontrol.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService service;

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @RequestBody UserRequest req,
                                               HttpSession session) {
        requireUserId(session);
        UserResponse updated = service.update(id, req);
        session.setAttribute("userId", updated.id());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
        requireUserId(session);
        service.delete(id);
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
