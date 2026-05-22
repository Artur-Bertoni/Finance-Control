package com.financecontrol.controller;

import com.financecontrol.dto.request.FinnyHistoryRequest;
import com.financecontrol.dto.response.AppNotificationResponse;
import com.financecontrol.service.AppNotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class AppNotificationController extends BaseController {

    private final AppNotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<AppNotificationResponse>> findAll(HttpSession session) {
        return ResponseEntity.ok(notificationService.findAll(requireUserId(session)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(HttpSession session) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(requireUserId(session))));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable @NonNull Long id,
                                           HttpSession session) {
        notificationService.markAsRead(requireUserId(session), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(HttpSession session) {
        notificationService.markAllAsRead(requireUserId(session));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/history")
    public ResponseEntity<AppNotificationResponse> saveHistory(@RequestBody FinnyHistoryRequest req,
                                                               HttpSession session) {
        return ResponseEntity.ok(notificationService.saveUserAction(requireUserId(session), req.message(), req.severity(), req.link()));
    }
}
