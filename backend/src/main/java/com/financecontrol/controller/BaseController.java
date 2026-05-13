package com.financecontrol.controller;

import com.financecontrol.exception.UnauthorizedException;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;

public abstract class BaseController {

    @NonNull
    protected Long requireUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) throw new UnauthorizedException("Não autenticado");
        return userId;
    }
}
