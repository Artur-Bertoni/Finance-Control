package com.financecontrol.service;

import com.financecontrol.entity.Account;
import com.financecontrol.entity.Category;
import com.financecontrol.entity.CategoryAlias;
import com.financecontrol.enums.AccountType;
import com.financecontrol.repository.AccountRepository;
import com.financecontrol.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;

    private record SeedCategory(String pt, String en, String es, String icon) {
        String name(String lang) {
            return switch (lang) {
                case "en" -> en;
                case "es" -> es;
                default   -> pt;
            };
        }
    }

    private static final List<SeedCategory> DEFAULT_CATEGORIES = List.of(
            new SeedCategory("Alimentação",       "Food",               "Alimentación",       "ph-fork-knife"),
            new SeedCategory("Transporte",        "Transportation",     "Transporte",         "ph-car"),
            new SeedCategory("Moradia",           "Housing",            "Vivienda",           "ph-house"),
            new SeedCategory("Saúde",             "Health",             "Salud",              "ph-heartbeat"),
            new SeedCategory("Lazer",             "Leisure",            "Ocio",               "ph-game-controller"),
            new SeedCategory("Educação",          "Education",          "Educación",          "ph-graduation-cap"),
            new SeedCategory("Compras",           "Shopping",           "Compras",            "ph-shopping-cart"),
            new SeedCategory("Contas e Serviços", "Bills & Utilities",  "Cuentas y Servicios","ph-receipt"),
            new SeedCategory("Salário",           "Salary",             "Salario",            "ph-hand-coins"),
            new SeedCategory("Outras Receitas",   "Other Income",       "Otros Ingresos",     "ph-currency-dollar")
    );

    private static final Map<String, String> WALLET_NAME = Map.of(
            "pt", "Carteira",
            "en", "Wallet",
            "es", "Billetera"
    );

    @Transactional
    public void seedDefaults(Long userId, String language) {
        if (userId == null) return;
        if (categoryRepository.countByUserId(userId) > 0 || accountRepository.countByUserId(userId) > 0) return;

        String lang = normalizeLang(language);
        LocalDateTime now = LocalDateTime.now();

        for (SeedCategory sc : DEFAULT_CATEGORIES) {
            Category c = new Category();
            c.setUserId(userId);
            c.setName(sc.name(lang));
            c.setIconKey(sc.icon());
            c.setSeeded(true);
            c.setCreatedAt(now);
            c.getAliases().add(new CategoryAlias(c, c.getName()));
            categoryRepository.save(c);
        }

        Account wallet = new Account();
        wallet.setUserId(userId);
        wallet.setName(WALLET_NAME.getOrDefault(lang, WALLET_NAME.get("pt")));
        wallet.setBalance(0.0);
        wallet.setType(AccountType.CHECKING);
        wallet.setIconKey("ph-wallet");
        wallet.setSeeded(true);
        wallet.setCreatedAt(now);
        accountRepository.save(wallet);
    }

    private String normalizeLang(String language) {
        if (language == null) return "pt";
        String l = language.toLowerCase();
        return (l.equals("en") || l.equals("es")) ? l : "pt";
    }
}
