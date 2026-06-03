package com.chessmate.chess_server.global.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void beforeEach() {
        jwtProvider = new JwtProvider(
                "test-secret-key-minimum-32-characters-long",
                1800000L,
                604800000L
        );
    }

    @Test
    void generationAccessToken() {
        String token = jwtProvider.generateAccessToken("test@test.com");

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void getEmail() {
        String email = "test@test.com";
        String token = jwtProvider.generateAccessToken(email);

        assertThat(jwtProvider.getEmail(token)).isEqualTo(email);
    }

    @Test
    void isValid_validToken() {
        String token = jwtProvider.generateAccessToken("test@test.com");

        assertThat(jwtProvider.isValid(token)).isTrue();
    }

    @Test
    void isValid_expirationToken() {
        JwtProvider expiredJwtProvider = new JwtProvider(
                "test-secret-key-minimum-32-characters-long",
                -1L,
                -1L
        );
        String token = expiredJwtProvider.generateAccessToken("test@test.com");

        assertThat(expiredJwtProvider.isValid(token)).isFalse();
    }

    @Test
    void isValid_invalidToken() {
        assertThat(jwtProvider.isValid("invalid token value")).isFalse();
    }
}