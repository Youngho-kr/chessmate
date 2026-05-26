package com.chessmate.chess_server.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReissueRequest {

    private String refreshToken;
}
