package com.financecontrol.service;

import com.financecontrol.entity.Transaction;
import com.financecontrol.enums.TransactionType;
import com.financecontrol.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
public class InstallmentMaturationScheduler {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final ZoneId zoneId;

    public InstallmentMaturationScheduler(TransactionRepository transactionRepository,
                                          AccountService accountService,
                                          @Value("${app.scheduler.timezone:America/Sao_Paulo}") String timezone) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.zoneId = ZoneId.of(timezone);
    }

    /** Aplica ao saldo, uma vez por dia, as parcelas futuras cuja data de vencimento já chegou. */
    @Transactional
    @Scheduled(cron = "0 10 0 * * *", zone = "${app.scheduler.timezone:America/Sao_Paulo}")
    public void matureDueInstallments() {
        LocalDate today = LocalDate.now(zoneId);
        List<Transaction> due = transactionRepository.findUnappliedDue(today);
        if (due.isEmpty()) return;

        log.info("Amadurecendo {} parcela(s) vencida(s) (data={})", due.size(), today);
        for (Transaction t : due) {
            try {
                applyToBalance(t);
            } catch (Exception e) {
                log.error("Erro ao amadurecer parcela id={}: {}", t.getId(), e.getMessage());
            }
        }
    }

    private void applyToBalance(Transaction t) {
        Long accountId = t.getAccount() != null ? t.getAccount().getId() : null;
        if (accountId == null) return;

        double delta = TransactionType.CREDIT == t.getType() ? t.getValue() : -t.getValue();
        accountService.patchBalance(accountId, delta);
        t.setApplied(true);
        transactionRepository.save(t);
    }
}
