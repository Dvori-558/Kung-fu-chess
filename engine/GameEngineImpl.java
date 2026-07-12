package engine;

import board.Board;
import realtime.RealTimeArbiter;
import rules.GameConfig;
import rules.RuleEngine;
import rules.MoveValidation;
import rules.StandardRuleEngine;
import models.Piece;

/** Coordinates validation, motion, and game state guards. */
public class GameEngineImpl implements GameEngine {
    private final Board board;
    private final GameConfig config;
    private final RuleEngine ruleEngine;
    private final RealTimeArbiter realTimeArbiter;
    private final WinManager winManager;

    public GameEngineImpl(Board board, GameConfig config) {
        this.board = board;
        this.config = config;
        this.ruleEngine = new StandardRuleEngine(config);
        this.winManager = new WinManager(config.getWinCondition());
        this.realTimeArbiter = new RealTimeArbiter(board, config.getWinCondition(), config.getPromotionRule());
    }

    @Override
    public MoveResult requestMove(int srcRow, int srcCol, int destRow, int destCol) {
        if (winManager.isGameOver()) {
            return MoveResult.rejected(MoveResult.GAME_OVER);
        }

        if (realTimeArbiter.hasActiveMotion()) {
            return MoveResult.rejected(MoveResult.MOTION_IN_PROGRESS);
        }

        MoveValidation validation = ruleEngine.validateMove(board, srcRow, srcCol, destRow, destCol);

        if (!validation.isValid()) {
            return MoveResult.rejected(validation.getReason());
        }

        Piece piece = board.getPieceAt(srcRow, srcCol);
        long durationMs = calculateMotionDuration(srcRow, srcCol, destRow, destCol);
        realTimeArbiter.startMotion(piece, srcRow, srcCol, destRow, destCol, durationMs);

        return MoveResult.accepted();
    }

    @Override
    public boolean requestJump(int row, int col) {
        if (winManager.isGameOver()) {
            return false;
        }
        return realTimeArbiter.startJump(row, col);
    }

    @Override
    public void pause(long durationMs) {
        realTimeArbiter.advanceTime(durationMs);
    }

    @Override
    public boolean hasActiveMotion() {
        return realTimeArbiter.hasActiveMotion();
    }

    @Override
    public boolean isGameOver() {
        return winManager.isGameOver();
    }

    @Override
    public GameSnapshot snapshot() {
        return new GameSnapshot(board, winManager.isGameOver());
    }

    /** Computes move duration from board distance. */
    private long calculateMotionDuration(int srcRow, int srcCol, int destRow, int destCol) {
        int rowDist = Math.abs(destRow - srcRow);
        int colDist = Math.abs(destCol - srcCol);
        int cells = Math.max(rowDist, colDist);
        if (cells == 0) cells = 1;
        return cells * 1000L;
    }
}
