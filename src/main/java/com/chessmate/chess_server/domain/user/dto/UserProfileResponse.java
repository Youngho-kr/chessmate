package com.chessmate.chess_server.domain.user.dto;

import com.chessmate.chess_server.domain.user.User;
import lombok.Getter;

@Getter
public class UserProfileResponse {

    private final String email;
    private final String nickname;
    private final int eloRating;

    public UserProfileResponse(User user) {

        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.eloRating = user.getEloRating();
    }
}
