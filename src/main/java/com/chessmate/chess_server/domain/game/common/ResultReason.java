package com.chessmate.chess_server.domain.game.common;

public enum ResultReason {
    // 승리
    CHECKMATE,                      // 체크메이트
    RESIGNATION,                    // 항복
    WIN_ON_TIME,                    // 시간승
    FORFEIT,                        // 반칙

    // 무승부
    STALEMATE,                      // 스테일메이트
    DRAW_BY_AGREEMENT,              // 합의무승부
    THREEFOLD_REPETITION,           // 3회 동형
    INSUFFICIENT_MATING_MATERIAL,   // 기물부족
    FIFTY_MOVE_RULE                 // 50수 규칙
}
