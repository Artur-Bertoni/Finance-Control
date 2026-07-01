package com.financecontrol.controller;

import com.financecontrol.entity.User;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.UnauthorizedException;
import com.financecontrol.service.EmailService;
import com.financecontrol.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/email")
public class AdminEmailController extends BaseController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/send-test")
    public ResponseEntity<Void> sendTestEmail(HttpSession session,
                                              @RequestParam(defaultValue = "WEEKLY") String type) {
        Long userId = requireUserId(session);
        User user = userService.findEntityById(userId);

        if (!user.isAdmin()) throw new UnauthorizedException("error.unauthorized");

        try {
            switch (type) {
                case "WEEKLY" -> emailService.sendTestEmail(user);
                case "VERIFICATION" -> emailService.sendTestVerificationEmail(user);
                case "FEEDBACK" -> emailService.sendTestFeedbackEmail(user);
                case "GOAL_DEADLINE_WARNING" -> emailService.sendTestGoalDeadlineEmail(user);
                default -> throw new IllegalArgumentException("unknown email type: " + type);
            }

            log.info("E-mail de teste '{}' enviado com sucesso para userId={}", type, user.getId());

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            throw new BusinessException("error.email.sendFailed");
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de teste para userId={} (tipo={}): {}", user.getId(), type, e.getMessage(), e);
            throw new BusinessException("error.email.sendFailed");
        }
    }
}
