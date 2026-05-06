package com.financecontrol.service;

import com.financecontrol.dto.request.TransactionRequest;
import com.financecontrol.dto.request.TransferRequest;
import com.financecontrol.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferService {

    private static final String TYPE_DEBIT = "debit";
    private static final String TYPE_CREDIT = "credit";

    private final TransactionService transactionService;

    @Transactional
    public void create(Long userId, TransferRequest req) {
        TransactionResponse origin = transactionService.create(userId, new TransactionRequest(
                req.originAccountId(), req.categoryId(), req.transactionLocaleId(),
                req.value(), req.date(), TYPE_DEBIT, 0, req.obs(), null));

        TransactionResponse destination = transactionService.create(userId, new TransactionRequest(
                req.destinationAccountId(), req.categoryId(), req.transactionLocaleId(),
                req.value(), req.date(), TYPE_CREDIT, 0, req.obs(), null));

        Long originId = origin.id();
        Long destinationId = destination.id();
        if (originId != null && destinationId != null) {
            transactionService.patchTransferPartner(originId, destinationId);
            transactionService.patchTransferPartner(destinationId, originId);
        }
    }
}
