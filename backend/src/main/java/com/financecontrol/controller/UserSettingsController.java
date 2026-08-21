package com.financecontrol.controller;

import com.financecontrol.dto.request.UserSettingsRequest;
import com.financecontrol.dto.response.UserSettingsResponse;
import com.financecontrol.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-settings")
public class UserSettingsController extends BaseController {

    private final UserSettingsService userSettingsService;

    @GetMapping
    public ResponseEntity<UserSettingsResponse> find() {
        return ResponseEntity.ok(userSettingsService.find(requireUserId()));
    }

    @PutMapping
    public ResponseEntity<UserSettingsResponse> update(@RequestBody UserSettingsRequest req) {
        return ResponseEntity.ok(userSettingsService.update(requireUserId(), req));
    }
}
