package com.financecontrol.controller;

import com.financecontrol.dto.request.TransferRequest;
import com.financecontrol.service.TransferService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController extends BaseController {

    private final TransferService service;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody TransferRequest req, HttpSession session) {
        service.create(requireUserId(session), req);
        return ResponseEntity.ok().build();
    }
}
