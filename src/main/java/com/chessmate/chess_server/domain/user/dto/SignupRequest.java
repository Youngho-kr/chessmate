package com.chessmate.chess_server.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignupRequest {

    private String email;
    private String password;
    private String nickname;
}
