package com.chessmate.chess_server.domain.guest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/guest")
public class GuestController {

    private final GuestSessionService guestSessionService;

    public GuestController(GuestSessionService guestSessionService) {
        this.guestSessionService = guestSessionService;
    }

    /*
     * 게스트 세션 발급
     * 비로그인 사용자가 최초 접속 시 호출
     */
    @PostMapping("/session")
    public ResponseEntity<Map<String, String>> issueSession() {
        String guestId = guestSessionService.issueSession();
        return ResponseEntity.ok(Map.of("guestId", guestId));
    }
}
