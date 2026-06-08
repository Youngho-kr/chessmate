package com.chessmate.chess_server.domain.analysis;

import com.chessmate.chess_server.global.config.StockfishConfig;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class StockfishService {

    private static final int MAX_SKILL_LEVEL = 20;

    private final StockfishConfig stockfishConfig;

    public StockfishService(StockfishConfig stockfishConfig) {
        this.stockfishConfig = stockfishConfig;
    }

    public StockfishResult evaluate(String fen) {
        return evaluate(fen, MAX_SKILL_LEVEL);
    }

    public StockfishResult evaluate(String fen, int skillLevel) {
        Process process = null;

        try {
            process = new ProcessBuilder(stockfishConfig.getPath())
                    .redirectErrorStream(true)
                    .start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            PrintWriter writer = new PrintWriter(process.getOutputStream(), true);

            reader.readLine();

            String line;
            writer.println("uci");
            while ((line = reader.readLine()) != null) {
                if (line.contains("uciok")) break;
            }

            // skillLevel이 20 미만일 때만 설정
            if (skillLevel < 20) {
                writer.println("setoption name skill Level value " + skillLevel);
            }

            writer.println("isready");
            while ((line = reader.readLine()) != null) {
                if (line.contains("readyok")) break;
            }

            writer.println("position fen " + fen);
            writer.println("go movetime 1000");

            int score = 0;
            String bestMove = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("info") && line.contains("score cp")) {
                    score = parseScore(line);
                }
                if (line.startsWith("bestmove")) {
                    String[] parts = line.split(" ");
                    if (parts.length > 1) {
                        String move = parts[1];
                        bestMove = move.equals("(none)") ? null : move; // Stockfish가 체크메이트 포지션에서 (none)을 반환함.
                    }
                    break;
                }
            }

            writer.println("quit");
            return new StockfishResult(score, bestMove);
        } catch (Exception e) {
            throw new RuntimeException("Stockfish 실행 실패: " + e.getMessage(), e);
        } finally {
            if (process != null) process.destroy();
        }
    }

    private int parseScore(String line) {
        try {
            String[] parts = line.split("score cp ");
            if (parts.length > 1) {
                return Integer.parseInt(parts[1].trim().split(" ")[0]);
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }
}