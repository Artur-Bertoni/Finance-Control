package com.financecontrol.repository;

import com.financecontrol.entity.CreditCardInvoicePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CreditCardInvoicePaymentRepository extends JpaRepository<CreditCardInvoicePayment, Long> {

    List<CreditCardInvoicePayment> findByAccount_Id(Long accountId);

    Optional<CreditCardInvoicePayment> findByAccount_IdAndReferenceMonth(Long accountId, String referenceMonth);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CreditCardInvoicePayment p WHERE p.account.id = :accountId")
    void deleteByAccountId(@Param("accountId") Long accountId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE CreditCardInvoicePayment p SET p.sourceAccount = NULL, p.paymentTransaction = NULL " +
           "WHERE p.sourceAccount.id = :accountId")
    void clearSourceAccount(@Param("accountId") Long accountId);
}
