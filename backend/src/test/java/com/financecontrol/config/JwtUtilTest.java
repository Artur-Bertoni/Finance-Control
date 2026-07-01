package com.financecontrol.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET =
            "this-is-a-very-long-and-secure-jwt-secret-key-for-tests-1234567890";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 7);
    }

    @Test
    void generateToken_produzTokenNaoVazio() {
        String token = jwtUtil.generateToken(42L);
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUserId_roundTrip() {
        String token = jwtUtil.generateToken(123L);
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(123L);
    }

    @Test
    void isValid_tokenValido_retornaTrue() {
        String token = jwtUtil.generateToken(1L);
        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void isValid_tokenInvalido_retornaFalse() {
        assertThat(jwtUtil.isValid("not.a.jwt")).isFalse();
    }

    @Test
    void isValid_tokenVazio_retornaFalse() {
        assertThat(jwtUtil.isValid("")).isFalse();
    }

    @Test
    void isValid_tokenAssinadoComOutraChave_retornaFalse() {
        JwtUtil other = new JwtUtil(
                "another-completely-different-secret-key-for-tests-0987654321xyz", 7);
        String token = other.generateToken(5L);
        assertThat(jwtUtil.isValid(token)).isFalse();
    }

    @Test
    void isValid_tokenExpirado_retornaFalse() {
        JwtUtil expired = new JwtUtil(SECRET, -1);
        String token = expired.generateToken(9L);
        assertThat(jwtUtil.isValid(token)).isFalse();
    }
    
    @Test
    void extractUserId_tokenMalformado_lancaJwtException() {
        assertThatThrownBy(() -> jwtUtil.extractUserId("not.a.jwt"))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    void extractUserId_tokenAssinadoComOutraChave_lancaJwtException() {
        JwtUtil other = new JwtUtil(
                "another-completely-different-secret-key-for-tests-0987654321xyz", 7);
        String token = other.generateToken(5L);

        assertThatThrownBy(() -> jwtUtil.extractUserId(token))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    void extractUserId_tokenExpirado_lancaJwtException() {
        JwtUtil expired = new JwtUtil(SECRET, -1);
        String token = expired.generateToken(9L);

        assertThatThrownBy(() -> jwtUtil.extractUserId(token))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    void isValid_tokenNulo_retornaFalse() {
        assertThat(jwtUtil.isValid(null)).isFalse();
    }

    @Test
    void setTokenCookie_secure_incluiAtributoSecure() {
        org.springframework.mock.web.MockHttpServletResponse response =
                new org.springframework.mock.web.MockHttpServletResponse();

        jwtUtil.setTokenCookie(response, "tok123", true);

        String header = response.getHeader("Set-Cookie");
        assertThat(header).isNotNull();
        assertThat(header).contains(JwtUtil.COOKIE_NAME + "=tok123");
        assertThat(header).contains("HttpOnly");
        assertThat(header).contains("Secure");
    }

    @Test
    void setTokenCookie_naoSecure_naoIncluiAtributoSecure() {
        org.springframework.mock.web.MockHttpServletResponse response =
                new org.springframework.mock.web.MockHttpServletResponse();

        jwtUtil.setTokenCookie(response, "tok123", false);

        String header = response.getHeader("Set-Cookie");
        assertThat(header).isNotNull();
        assertThat(header).doesNotContain("Secure");
    }

    @Test
    void clearTokenCookie_setaMaxAgeZero() {
        org.springframework.mock.web.MockHttpServletResponse response =
                new org.springframework.mock.web.MockHttpServletResponse();

        jwtUtil.clearTokenCookie(response);

        String header = response.getHeader("Set-Cookie");
        assertThat(header).isNotNull();
        assertThat(header).contains("Max-Age=0");
    }

    @Test
    void extractBearerToken_comHeaderBearer_retornaToken() {
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer tok-abc");

        assertThat(jwtUtil.extractBearerToken(request)).isEqualTo("tok-abc");
    }

    @Test
    void extractBearerToken_semHeader_retornaNull() {
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();

        assertThat(jwtUtil.extractBearerToken(request)).isNull();
    }

    @Test
    void extractBearerToken_headerSemPrefixoBearer_retornaNull() {
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();
        request.addHeader("Authorization", "tok-sem-prefixo");

        assertThat(jwtUtil.extractBearerToken(request)).isNull();
    }

    @Test
    void extractTokenFromRequest_semCookies_retornaNull() {
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();

        assertThat(jwtUtil.extractTokenFromCookie(request)).isNull();
    }

    @Test
    void extractTokenFromRequest_comCookieAuth_retornaValor() {
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();
        request.setCookies(
                new jakarta.servlet.http.Cookie("other", "x"),
                new jakarta.servlet.http.Cookie(JwtUtil.COOKIE_NAME, "tok999"));

        assertThat(jwtUtil.extractTokenFromCookie(request)).isEqualTo("tok999");
    }

    @Test
    void extractTokenFromRequest_semCookieAuth_retornaNull() {
        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("other", "x"));

        assertThat(jwtUtil.extractTokenFromCookie(request)).isNull();
    }
}
