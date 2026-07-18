package engine;

import board.Board;
import realtime.RealTimeArbiter;
import realtime.Motion;
import realtime.AirborneJump;
import rules.GameConfig;
import rules.RuleEngine;
import rules.MoveValidation;
import rules.StandardRuleEngine;
import models.Piece;
import java.util.List;

/** Coordinates validation, motion, and game state guards. */
public class GameEngineImpl implements GameEngine {
    private final Board board;
    private final GameConfig config;
    private final RuleEngine ruleEngine;
    private final RealTimeArbiter realTimeArbiter;
    private final WinManager winManager;

    public GameEngineImpl(Board board, GameConfig config) {
        this.board = board;
        // Store config for potential future use; currently delegated to components.
        // If not needed, it can be removed in a future refactor.
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

        if (realTimeArbiter.hasActiveMotion() && !realTimeArbiter.isIncomingDestination(srcRow, srcCol)) {
            return MoveResult.rejected(MoveResult.MOTION_IN_PROGRESS);
        }

        if (realTimeArbiter.isPieceResting(srcRow, srcCol)) {
            return MoveResult.rejected(MoveResult.REST_IN_PROGRESS);
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
        if (realTimeArbiter.isPieceResting(row, col)) {
            return false;
        }
        return realTimeArbiter.startJump(row, col, 1000L);
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
    public List<Motion> getActiveMotions() {
        return realTimeArbiter.getActiveMotions();
    }

    @Override
    public AirborneJump getAirborneJump() {
        return realTimeArbiter.getAirborneJump();
    }

    @Override
    public boolean isGameOver() {
        return winManager.isGameOver();
    }

    @Override
    public GameSnapshot snapshot() {
        return new GameSnapshot(board, winManager.isGameOver());
    }

    /** Provides access to game configuration for introspection. */
    public GameConfig getConfig() {
        return config;
    }

    /** Returns winner color when game is over, or null if no winner yet. */
    public Character getWinnerColor() {
        return winManager.getWinner();
    }

    /** Computes move duration from board distance. */
    private long calculateMotionDuration(int srcRow, int srcCol, int destRow, int destCol) {
        int rowDist = Math.abs(destRow - srcRow);
        int colDist = Math.abs(destCol - srcCol);
        int cells = Math.max(rowDist, colDist);
        if (cells == 0) cells = 1;

        Piece piece = board.getPieceAt(srcRow, srcCol);
        double speedCellsPerSec = piece == null ? 0.0 : config.getMoveSpeedCellsPerSec(piece.getType());
        if (speedCellsPerSec > 0.0) {
            long ms = Math.round((cells / speedCellsPerSec) * 1000.0);
            return Math.max(120L, ms);
        }

        return cells * 1000L;
    }
}
