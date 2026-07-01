package com.financecontrol.repository;

import com.financecontrol.entity.CreditCardInvoicePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditCardInvoicePaymentRepository extends JpaRepository<CreditCardInvoicePayment, Long> {

    List<CreditCardInvoicePayment> findByAccount_Id(Long accountId);

    Optional<CreditCardInvoicePayment> findByAccount_IdAndReferenceMonth(Long accountId, String referenceMonth);
}
