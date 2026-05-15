package com.financecontrol.service;

import com.financecontrol.dto.request.TransactionRequest;
import com.financecontrol.dto.request.TransferRequest;
import com.financecontrol.dto.response.TransactionResponse;
import com.financecontrol.entity.Account;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.exception.ResourceNotFoundException;
import com.financecontrol.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransactionService transactionService;
    private final AccountRepository  accountRepository;

    @Transactional
    public void create(Long userId, TransferRequest req) {
        if (Objects.equals(req.originAccountId(), req.destinationAccountId()))
            throw new BusinessException("error.transfer.sameAccount");

        Account origin = accountRepository.findById(req.originAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound.account"));
        double balance = origin.getBalance() != null ? origin.getBalance() : 0.0;
        if (balance < req.value())
            throw new BusinessException("error.transfer.insufficientBalance");

        TransactionResponse originTx = transactionService.create(userId, new TransactionRequest(
                req.originAccountId(), req.categoryId(), req.transactionLocaleId(),
                req.value(), req.date(), TransactionType.DEBIT, null, req.obs(), null));

        TransactionResponse destinationTx = transactionService.create(userId, new TransactionRequest(
                req.destinationAccountId(), req.categoryId(), req.transactionLocaleId(),
                req.value(), req.date(), TransactionType.CREDIT, null, req.obs(), null));

        Long originId = originTx.id();
        Long destinationId = destinationTx.id();
        if (originId != null && destinationId != null) {
            transactionService.patchTransferPartner(originId, destinationId);
            transactionService.patchTransferPartner(destinationId, originId);
        }
    }
}
