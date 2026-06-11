package com.chessmate.chess_server.domain.match;

import lombok.Getter;

@Getter
public class InviteCode {

    private final String code;
    private final String gameId;
    private final String hostId;
    private final long createdAt;

    public InviteCode(String code, String gameId, String hostId, long createdAt) {
        this.code = code;
        this.gameId = gameId;
        this.hostId = hostId;
        this.createdAt = createdAt;
    }
}
