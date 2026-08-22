package com.financecontrol.service;

import com.financecontrol.dto.request.UserSettingsRequest;
import com.financecontrol.dto.response.UserSettingsResponse;
import com.financecontrol.entity.UserSettings;
import com.financecontrol.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.financecontrol.service.HistoryService.ENTITY_USER;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final UserSettingsRepository repository;
    private final HistoryService historyService;

    /** Retorna as configuracoes do usuario, criando o registro padrao na primeira leitura. */
    @Transactional
    public UserSettings getOrCreate(@NonNull Long userId) {
        return repository.findByUserId(userId).orElseGet(() -> {
            UserSettings settings = new UserSettings();
            settings.setUserId(userId);
            settings.setUpdatedAt(LocalDateTime.now(ZONE));
            return repository.save(settings);
        });
    }

    public UserSettingsResponse find(@NonNull Long userId) {
        return UserSettingsResponse.from(getOrCreate(userId));
    }

    /** Usuario novo comeca com tudo desligado: quem liga as funcionalidades e a escolha de perfil no onboarding. */
    @Transactional
    public void seedDisabled(@NonNull Long userId) {
        if (repository.findByUserId(userId).isPresent()) return;

        UserSettings settings = new UserSettings();
        settings.setUserId(userId);
        settings.setReportsEnabled(false);
        settings.setBudgetsEnabled(false);
        settings.setGoalsEnabled(false);
        settings.setFinnyEnabled(false);
        settings.setStatementImportEnabled(false);
        settings.setInstitutionsEnabled(false);
        settings.setLocalesEnabled(false);
        settings.setEmailsEnabled(false);
        settings.setUpdatedAt(LocalDateTime.now(ZONE));

        repository.save(settings);
    }

    /** Aplica os toggles informados, registrando as mudancas no historico do usuario. */
    @Transactional
    public UserSettingsResponse update(@NonNull Long userId,
                                       UserSettingsRequest req) {
        UserSettings settings = getOrCreate(userId);
        Map<String, String[]> diff = new LinkedHashMap<>();

        apply(diff, "reportsEnabled", req.reportsEnabled(), settings, UserSettings::isReportsEnabled, UserSettings::setReportsEnabled);
        apply(diff, "budgetsEnabled", req.budgetsEnabled(), settings, UserSettings::isBudgetsEnabled, UserSettings::setBudgetsEnabled);
        apply(diff, "goalsEnabled", req.goalsEnabled(), settings, UserSettings::isGoalsEnabled, UserSettings::setGoalsEnabled);
        apply(diff, "finnyEnabled", req.finnyEnabled(), settings, UserSettings::isFinnyEnabled, UserSettings::setFinnyEnabled);
        apply(diff, "statementImportEnabled", req.statementImportEnabled(), settings, UserSettings::isStatementImportEnabled, UserSettings::setStatementImportEnabled);
        apply(diff, "institutionsEnabled", req.institutionsEnabled(), settings, UserSettings::isInstitutionsEnabled, UserSettings::setInstitutionsEnabled);
        apply(diff, "localesEnabled", req.localesEnabled(), settings, UserSettings::isLocalesEnabled, UserSettings::setLocalesEnabled);
        apply(diff, "emailsEnabled", req.emailsEnabled(), settings, UserSettings::isEmailsEnabled, UserSettings::setEmailsEnabled);

        if (diff.isEmpty()) return UserSettingsResponse.from(settings);

        settings.setUpdatedAt(LocalDateTime.now(ZONE));
        UserSettings saved = repository.save(settings);
        historyService.recordChanges(ENTITY_USER, userId, userId, diff);

        return UserSettingsResponse.from(saved);
    }

    /** Le um toggle sem criar registro: usuario sem configuracoes salvas mantem tudo habilitado. */
    public boolean isEnabled(Long userId,
                             Function<UserSettings, Boolean> getter) {
        return repository.findByUserId(userId).map(getter).orElse(Boolean.TRUE);
    }

    public boolean goalsEnabled(Long userId)   { return isEnabled(userId, UserSettings::isGoalsEnabled); }
    public boolean budgetsEnabled(Long userId) { return isEnabled(userId, UserSettings::isBudgetsEnabled); }
    public boolean finnyEnabled(Long userId)   { return isEnabled(userId, UserSettings::isFinnyEnabled); }
    public boolean emailsEnabled(Long userId)  { return isEnabled(userId, UserSettings::isEmailsEnabled); }
    public boolean localesEnabled(Long userId) { return isEnabled(userId, UserSettings::isLocalesEnabled); }
    public boolean institutionsEnabled(Long userId) { return isEnabled(userId, UserSettings::isInstitutionsEnabled); }

    @SuppressWarnings("null")
    private void apply(Map<String, String[]> diff,
                       String field,
                       Boolean requested,
                       UserSettings settings,
                       Function<UserSettings, Boolean> getter,
                       BiConsumer<UserSettings, Boolean> setter) {
        if (requested == null) return;

        boolean current = Boolean.TRUE.equals(getter.apply(settings));
        if (current == requested) return;

        diff.put(field, new String[] { String.valueOf(current), String.valueOf(requested) });
        setter.accept(settings, requested);
    }
}
