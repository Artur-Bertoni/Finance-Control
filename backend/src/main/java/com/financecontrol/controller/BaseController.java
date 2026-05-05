package com.financecontrol.controller;

import com.financecontrol.exception.UnauthorizedException;
import jakarta.servlet.http.HttpSession;

public abstract class BaseController {

    protected Long requireUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) throw new UnauthorizedException("Não autenticado");
        return userId;
    }
}
