package com.chessmate.chess_server.domain.game.play.dto;

import lombok.Getter;

@Getter
public class DrawDeclineResponse {

    private  final String type = "DRAW_DECLINE";
}
