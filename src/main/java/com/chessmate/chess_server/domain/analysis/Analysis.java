package com.chessmate.chess_server.domain.analysis;

import com.chessmate.chess_server.domain.game.record.GameRecord;
import com.chessmate.chess_server.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Analysis {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_record_id")
    private GameRecord gameRecord;

    @Column(nullable = false)
    private double whiteAccuracy;

    @Column(nullable = false)
    private double blackAccuracy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String tree;

    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        analyzedAt = LocalDateTime.now();
    }

    public Analysis(User user, GameRecord gameRecord,
                    double whiteAccuracy, double blackAccuracy, String tree) {
        this.user = user;
        this.gameRecord = gameRecord;
        this.whiteAccuracy = whiteAccuracy;
        this.blackAccuracy = blackAccuracy;
        this.tree = tree;
    }
}
