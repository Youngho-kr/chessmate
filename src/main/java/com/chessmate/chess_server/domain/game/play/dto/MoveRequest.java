package com.chessmate.chess_server.domain.game.play.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MoveRequest {

    private String from;
    private String to;
}
