package com.financecontrol.controller;

import com.financecontrol.entity.User;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.exception.UnauthorizedException;
import com.financecontrol.repository.UserRepository;
import com.financecontrol.service.EmailService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/email")
@RequiredArgsConstructor
public class AdminEmailController extends BaseController {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @PostMapping("/send-test")
    public ResponseEntity<Void> sendTestEmail(HttpSession session) {
        Long userId = requireUserId(session);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.user"));
        if (!user.isAdmin()) throw new UnauthorizedException("error.unauthorized");
        emailService.sendWeeklyReminder(user);
        return ResponseEntity.accepted().build();
    }
}
