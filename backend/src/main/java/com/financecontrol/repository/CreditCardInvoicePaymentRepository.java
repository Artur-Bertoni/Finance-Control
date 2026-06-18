package com.financecontrol.repository;

import com.financecontrol.entity.CreditCardInvoicePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditCardInvoicePaymentRepository extends JpaRepository<CreditCardInvoicePayment, Long> {

    List<CreditCardInvoicePayment> findByAccountId(Long accountId);

    Optional<CreditCardInvoicePayment> findByAccountIdAndReferenceMonth(Long accountId, String referenceMonth);
}
