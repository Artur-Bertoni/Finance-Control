package com.financecontrol.controller;

import com.financecontrol.dto.request.StatementConfirmRequest;
import com.financecontrol.dto.response.ImportResult;
import com.financecontrol.dto.response.ParsedTransactionResponse;
import com.financecontrol.service.StatementImportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statements")
public class StatementImportController extends BaseController {

    private final StatementImportService statementImportService;

    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ParsedTransactionResponse>> preview(@RequestParam MultipartFile file,
                                                                   HttpSession session) {
        return ResponseEntity.ok(statementImportService.previewStatement(requireUserId(session), file));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ImportResult> confirm(@RequestBody StatementConfirmRequest req,
                                                HttpSession session) {
        return ResponseEntity.ok(statementImportService.confirmImport(requireUserId(session), req.accountId(), req.rows()));
    }
}
