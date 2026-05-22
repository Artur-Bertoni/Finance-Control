package com.financecontrol.controller;

import com.financecontrol.exception.UnauthorizedException;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public abstract class BaseController {

    @NonNull
    protected Long requireUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Long))
            throw new UnauthorizedException("Não autenticado");
        
        return Objects.requireNonNull((Long) auth.getPrincipal());
    }

    @NonNull
    protected Long requireUserId(@SuppressWarnings("unused") HttpSession session) {
        return requireUserId();
    }
}
