package com.chessmate.chess_server.domain.guest;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GuestSessionServiceTest {

    @Autowired
    private GuestSessionService guestSessionService;

    @Test
    @DisplayName("게스트 세션 발급 시 UUID 형식의 guestId 반환")
    void issueSession() {
        String guestId = guestSessionService.issueSession();

        assertThat(guestId).isNotNull();
        assertThat(guestId).isNotBlank();
    }

    @Test
    @DisplayName("guestId 유효성 검증")
    void isValid() {
        String guestId = guestSessionService.issueSession();

        assertThat(guestSessionService.isValid(guestId)).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 guestId 유효성 검증 실패")
    void isValid_invalidGuestId() {
        assertThat(guestSessionService.isValid("invalid-id")).isFalse();
    }

    @Test
    @DisplayName("고유한 guestId 발급")
    void issueSession_unique() {
        String guestId1 = guestSessionService.issueSession();
        String guestId2 = guestSessionService.issueSession();

        assertThat(guestId1).isNotEqualTo(guestId2);
    }
}