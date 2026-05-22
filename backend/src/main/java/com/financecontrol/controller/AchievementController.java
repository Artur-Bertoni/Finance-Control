package com.financecontrol.controller;

import com.financecontrol.dto.response.AchievementResponse;
import com.financecontrol.service.AchievementService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/achievements")
public class AchievementController extends BaseController {

    private final AchievementService achievementService;

    @GetMapping
    public ResponseEntity<List<AchievementResponse>> list(HttpSession session) {
        return ResponseEntity.ok(achievementService.checkAndList(requireUserId(session)));
    }
}
