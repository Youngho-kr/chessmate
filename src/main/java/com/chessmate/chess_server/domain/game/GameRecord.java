package com.chessmate.chess_server.domain.game;

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

    public GameRecord(String pgn, GameType gameType, String externalId, String source) {
        this.pgn = pgn;
        this.gameType = gameType;
        this.externalId = externalId;
        this.source = source;
    }

    public void finish(ResultReason resultReason) {
        this.resultReason = resultReason;
    }
}
