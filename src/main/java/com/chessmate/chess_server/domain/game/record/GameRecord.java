package com.chessmate.chess_server.domain.game.record;

import com.chessmate.chess_server.domain.game.common.GameType;
import com.chessmate.chess_server.domain.game.common.ResultReason;
import com.chessmate.chess_server.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String pgn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameType gameType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "white_player_id", nullable = true)
    private User whitePlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "black_player_id", nullable = true)
    private User blackPlayer;

    @Column(nullable = true)
    private String source;

    @Column(nullable = true)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ResultReason resultReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void OnCreated() {
        createdAt = LocalDateTime.now();
    }

    public GameRecord(String pgn, GameType gameType,
                      User whitePlayer, User blackPlayer,
                      ResultReason resultReason) {
        this.pgn = pgn;
        this.gameType = gameType;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.resultReason = resultReason;
    }

    public GameRecord(String pgn, GameType gameType,
                      String source, String externalId) {
        this.pgn = pgn;
        this.gameType = gameType;
        this.source = source;
        this.externalId = externalId;
    }
}
