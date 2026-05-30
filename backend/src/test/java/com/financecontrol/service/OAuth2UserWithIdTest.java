package com.financecontrol.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class OAuth2UserWithIdTest {

    private OAuth2User delegate() {
        Map<String, Object> attrs = Map.of("email", "user@test.com", "sub", "123");
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new DefaultOAuth2User(authorities, attrs, "sub");
    }

    @Test
    void getters_retornamValoresDoConstrutor() {
        OAuth2UserWithId user = new OAuth2UserWithId(delegate(), 42L, true);

        assertThat(user.getUserId()).isEqualTo(42L);
        assertThat(user.isWasLinkFlow()).isTrue();
    }

    @Test
    void wasLinkFlowFalse_refleteValor() {
        OAuth2UserWithId user = new OAuth2UserWithId(delegate(), 7L, false);

        assertThat(user.isWasLinkFlow()).isFalse();
        assertThat(user.getUserId()).isEqualTo(7L);
    }

    @Test
    void getAttributes_delegaParaUsuarioInterno() {
        OAuth2UserWithId user = new OAuth2UserWithId(delegate(), 1L, false);

        assertThat(user.getAttributes())
                .containsEntry("email", "user@test.com")
                .containsEntry("sub", "123");
    }

    @Test
    void getAuthorities_delegaParaUsuarioInterno() {
        OAuth2UserWithId user = new OAuth2UserWithId(delegate(), 1L, false);

        assertThat(user.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void getName_delegaParaUsuarioInterno() {
        OAuth2UserWithId user = new OAuth2UserWithId(delegate(), 1L, false);

        // name key is "sub" whose value is "123"
        assertThat(user.getName()).isEqualTo("123");
    }
}
