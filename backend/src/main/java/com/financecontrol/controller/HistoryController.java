package com.financecontrol.controller;

import com.financecontrol.dto.response.ChangeGroupResponse;
import com.financecontrol.service.HistoryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/change-history")
public class HistoryController extends BaseController {

    private final HistoryService changeHistoryService;

    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<List<ChangeGroupResponse>> getHistory(@PathVariable String entityType,
                                                                @PathVariable Long entityId,
                                                                HttpSession session) {
        requireUserId(session);
        return ResponseEntity.ok(changeHistoryService.getHistory(entityType, entityId));
    }
}
