package com.financecontrol.controller;

import com.financecontrol.dto.request.UserFeedbackRequest;
import com.financecontrol.dto.response.UserFeedbackResponse;
import com.financecontrol.entity.User;
import com.financecontrol.exception.UnauthorizedException;
import com.financecontrol.repository.UserRepository;
import com.financecontrol.service.UserFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserFeedbackController extends BaseController {

    private final UserFeedbackService feedbackService;
    private final UserRepository      userRepository;

    @PostMapping("/feedback")
    public ResponseEntity<UserFeedbackResponse> submit(@RequestBody UserFeedbackRequest req) {
        return ResponseEntity.ok(feedbackService.submit(requireUserId(), req));
    }

    @GetMapping("/admin/feedbacks")
    public ResponseEntity<Page<UserFeedbackResponse>> findAll(
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "20") int    size,
            @RequestParam(required = false)    String type) {
        User current = userRepository.findById(requireUserId())
                .orElseThrow(() -> new UnauthorizedException("Não autenticado"));
        if (!current.isAdmin()) throw new UnauthorizedException("Acesso negado");
        return ResponseEntity.ok(feedbackService.findAll(page, size, type));
    }
}
