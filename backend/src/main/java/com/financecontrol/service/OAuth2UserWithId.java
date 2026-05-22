package com.financecontrol.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class OAuth2UserWithId implements OAuth2User {

    private final OAuth2User delegate;
    private final Long userId;
    private final boolean wasLinkFlow;

    public OAuth2UserWithId(OAuth2User delegate, Long userId, boolean wasLinkFlow) {
        this.delegate     = delegate;
        this.userId       = userId;
        this.wasLinkFlow  = wasLinkFlow;
    }

    public Long getUserId() { return userId; }
    public boolean isWasLinkFlow() { return wasLinkFlow; }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override public String getName() {
        return delegate.getName();
    }
}
