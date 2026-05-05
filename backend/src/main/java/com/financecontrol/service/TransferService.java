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

    private final TransactionService transactionService;

    @Transactional
    public void create(Long userId, TransferRequest req) {
        TransactionResponse origin = transactionService.create(userId, new TransactionRequest(
                req.originAccountId(), req.categoryId(), req.transactionLocaleId(),
                req.value(), req.date(), "debit", 0, req.obs(), null));

        TransactionResponse destination = transactionService.create(userId, new TransactionRequest(
                req.destinationAccountId(), req.categoryId(), req.transactionLocaleId(),
                req.value(), req.date(), "credit", 0, req.obs(), null));

        transactionService.patchTransferPartner(origin.id(), destination.id());
        transactionService.patchTransferPartner(destination.id(), origin.id());
    }
}
