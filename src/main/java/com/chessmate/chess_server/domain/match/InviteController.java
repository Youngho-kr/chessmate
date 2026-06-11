package com.chessmate.chess_server.domain.match;

import com.chessmate.chess_server.domain.guest.GuestSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/invite")
public class InviteController {

    private final InviteService inviteService;
    private final GuestSessionService guestSessionService;

    public InviteController(InviteService inviteService,
                            GuestSessionService guestSessionService) {
        this.inviteService = inviteService;
        this.guestSessionService = guestSessionService;
    }

    /**
     * 초대 코드 생성
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createInvite(
            @RequestHeader(value = "GuestID", required = false) String guestId,
            Principal principal) {

        return guestSessionService.resolveId(principal, guestId)
                .map(hostId -> {
                    String code = inviteService.createInvite(hostId);
                    return ResponseEntity.ok(Map.of("code", code));
                })
                .orElse(ResponseEntity.status(401).build());
    }

    /**
     * 초대 코드로 참가
     * 유효하지 않은 코드라면 404 반환
     */
    @PostMapping("/join/{code}")
    public ResponseEntity<Map<String, String>> joinInvite(
            @PathVariable String code,
            @RequestHeader(value = "GuestId", required = false) String guestId,
            Principal principal) {

        return guestSessionService.resolveId(principal, guestId)
                .flatMap(participantId -> inviteService.joinByCode(code, participantId))
                .map(gameId -> ResponseEntity.ok(Map.of("gameId", gameId)))
                .orElse(ResponseEntity.notFound().build());
    }
}
