package com.financecontrol.controller;

import com.financecontrol.dto.request.FinnyFeedbackRequest;
import com.financecontrol.dto.response.FinnyStatsResponse;
import com.financecontrol.dto.response.FinnyTipResponse;
import com.financecontrol.service.finny.FinnyAgentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/finny")
public class FinnyController extends BaseController {

    private final FinnyAgentService agentService;

    @GetMapping("/tips")
    public ResponseEntity<List<FinnyTipResponse>> getTips(HttpSession session) {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        return ResponseEntity.ok(agentService.generateTips(requireUserId(session), lang));
    }

    @PostMapping("/tips/{id}/shown")
    public ResponseEntity<FinnyTipResponse> markShown(@PathVariable @NonNull Long id,
                                                      HttpSession session) {
        return ResponseEntity.ok(agentService.markShown(requireUserId(session), id));
    }

    @GetMapping("/tips/history")
    public ResponseEntity<List<FinnyTipResponse>> getHistory(HttpSession session) {
        return ResponseEntity.ok(agentService.getHistory(requireUserId(session)));
    }

    @GetMapping("/tips/stats")
    public ResponseEntity<FinnyStatsResponse> getStats(HttpSession session) {
        return ResponseEntity.ok(agentService.getStats(requireUserId(session)));
    }

    @PostMapping("/tips/{id}/feedback")
    public ResponseEntity<FinnyTipResponse> feedback(@PathVariable @NonNull Long id,
                                                     @RequestBody FinnyFeedbackRequest req,
                                                     HttpSession session) {
        return ResponseEntity.ok(agentService.recordFeedback(requireUserId(session), id, req.feedback()));
    }
}
