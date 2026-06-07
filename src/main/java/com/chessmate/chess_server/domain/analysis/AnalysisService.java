package com.chessmate.chess_server.domain.analysis;

import com.chessmate.chess_server.domain.game.common.PlayerColor;
import com.chessmate.chess_server.domain.game.record.GameRecord;
import com.chessmate.chess_server.domain.game.record.GameRecordRepository;
import com.chessmate.chess_server.domain.user.User;
import com.chessmate.chess_server.domain.user.UserRepository;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisService {

    private final StockfishService stockfishService;
    private final GameRecordRepository gameRecordRepository;
    private final UserRepository userRepository;
    private final MoveClassifier moveClassifier;
    private final AccuracyCalculator accuracyCalculator;
    private final ObjectMapper objectMapper;
    private final AnalysisRepository analysisRepository;

    public AnalysisService(StockfishService stockfishService,
                           GameRecordRepository gameRecordRepository,
                           UserRepository userRepository,
                           MoveClassifier moveClassifier,
                           AccuracyCalculator accuracyCalculator,
                           ObjectMapper objectMapper,
                           AnalysisRepository analysisRepository) {
        this.stockfishService = stockfishService;
        this.gameRecordRepository = gameRecordRepository;
        this.userRepository = userRepository;
        this.moveClassifier = moveClassifier;
        this.accuracyCalculator = accuracyCalculator;
        this.objectMapper = objectMapper;
        this.analysisRepository = analysisRepository;
    }

    @Transactional
    public void analyze(Long gameRecordId, Long userId) {
        if (analysisRepository.existsByGameRecordIdAndUserId(gameRecordId, userId)) {
            throw new IllegalArgumentException("이미 분석된 게임입니다.");
        }

        GameRecord gameRecord = gameRecordRepository.findById(gameRecordId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게임입니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<String> moves = parseMoves(gameRecord.getPgn());
        List<AnalysisNode> nodes = new ArrayList<>();

        Board board = new Board();
        int prevScore = 0;
        String parentId = null;

        for (int i = 0; i < moves.size(); i++) {
            String from = moves.get(i).substring(0, 2);
            String to = moves.get(i).substring(2, 4);

            Move move = new Move(
                    Square.fromValue(from.toUpperCase()),
                    Square.fromValue(to.toUpperCase())
            );

            board.doMove(move);
            String fen = board.getFen();
            StockfishResult result = stockfishService.evaluate(fen);

            PlayerColor color = i % 2 == 0 ? PlayerColor.WHITE : PlayerColor.BLACK;
            String nodeId = "node-" + i;
            AnalysisNode node = new AnalysisNode(
                    nodeId,
                    parentId,
                    i / 2 + 1,
                    i % 2 == 0 ? PlayerColor.WHITE : PlayerColor.BLACK,
                    moves.get(i),
                    fen,
                    result.getBestMove(),
                    result.getEvalScore(),
                    true
            );

            node.setClassification(
                    moveClassifier.classify(prevScore, result.getEvalScore(), color)
            );
            nodes.add(node);
            parentId = nodeId;
            prevScore = result.getEvalScore();
        }

        // 저장
        double whiteAccuracy = accuracyCalculator.calculate(nodes, PlayerColor.WHITE);
        double blackAccuracy = accuracyCalculator.calculate(nodes, PlayerColor.BLACK);

        try {
            String tree = objectMapper.writeValueAsString(nodes);
            analysisRepository.save(
                    new Analysis(user, gameRecord, whiteAccuracy, blackAccuracy, tree));
        } catch (Exception e) {
            throw new RuntimeException("분석 저장 실패", e);
        }
    }

    public Analysis getAnalysis(Long gameRecordId, Long userId) {
        return analysisRepository.findByGameRecordIdAndUserId(gameRecordId, userId)
                .orElseThrow(() -> new IllegalArgumentException("분석 결과가 없습니다."));
    }

    private List<String> parseMoves(String pgn) {
        List<String> moves = new ArrayList<>();
        if (pgn == null || pgn.isBlank()) return moves;
        for (String move : pgn.split(" ")) {
            if (!move.isBlank()) moves.add(move);
        }
        return moves;
    }
}
