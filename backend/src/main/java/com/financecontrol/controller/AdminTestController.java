package com.financecontrol.controller;

import com.financecontrol.entity.User;
import com.financecontrol.exception.UnauthorizedException;
import com.financecontrol.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/test")
public class AdminTestController extends BaseController {

    private final UserService userService;

    @PostMapping("/mark-email-unverified")
    public ResponseEntity<Void> markEmailUnverified() {
        Long userId = requireUserId();
        User user = userService.findEntityById(userId);
        if (!user.isAdmin()) throw new UnauthorizedException("error.unauthorized");

        userService.markEmailUnverified(userId);
        return ResponseEntity.noContent().build();
    }
}
