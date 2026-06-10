package com.chessmate.chess_server.domain.game.record;

import com.chessmate.chess_server.domain.game.record.dto.GameDetailResponse;
import com.chessmate.chess_server.domain.game.record.dto.GameSummaryResponse;
import com.chessmate.chess_server.domain.user.User;
import com.chessmate.chess_server.domain.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
public class GameRecordController {

    private final GameRecordService gameRecordService;
    private final UserRepository userRepository;

    public GameRecordController(GameRecordService gameRecordService, UserRepository userRepository) {
        this.gameRecordService = gameRecordService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<GameSummaryResponse>> getGameList(
            @AuthenticationPrincipal String email,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return ResponseEntity.ok(gameRecordService.getGameList(user.getId(), pageable));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDetailResponse> getGameDetail(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameRecordService.getGameDetail(gameId));
    }
}
