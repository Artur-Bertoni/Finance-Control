package com.financecontrol.annotation;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithLongPrincipalSecurityContextFactory
        implements WithSecurityContextFactory<WithLongPrincipal> {

    @Override
    public SecurityContext createSecurityContext(WithLongPrincipal annotation) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                annotation.value(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));
        return ctx;
    }
}
