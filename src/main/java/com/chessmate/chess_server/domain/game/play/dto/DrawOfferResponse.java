package com.chessmate.chess_server.domain.game.play.dto;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import lombok.Getter;

@Getter
public class DrawOfferResponse {

    private final String type = "DRAW_OFFER";
    private final PlayerColor offerPlayer;

    public DrawOfferResponse(PlayerColor offerPlayer) {
        this.offerPlayer = offerPlayer;
    }
}
