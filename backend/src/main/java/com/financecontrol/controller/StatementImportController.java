package com.financecontrol.controller;

import com.financecontrol.dto.response.ImportResult;
import com.financecontrol.service.StatementImportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/statements")
@RequiredArgsConstructor
public class StatementImportController extends BaseController {

    private final StatementImportService statementImportService;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> statementImport(
            @RequestParam MultipartFile file,
            @RequestParam Long accountId,
            HttpSession session) {
        return ResponseEntity.ok(statementImportService.statementImport(
                requireUserId(session), accountId, file));
    }
}
