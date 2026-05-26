package com.chessmate.chess_server.domain.game;

import com.chessmate.chess_server.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_game")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGame {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_record_id", nullable = false)
    private GameRecord gameRecord;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private PlayerColor color;

    public UserGame(User user, GameRecord gameRecord, PlayerColor color) {
        this.user = user;
        this.gameRecord = gameRecord;
        this.color = color;
    }
}
