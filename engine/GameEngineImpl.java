package engine;

import board.Board;
import realtime.RealTimeArbiter;
import rules.GameConfig;
import rules.RuleEngine;
import rules.MoveValidation;
import rules.StandardRuleEngine;
import rules.WinCondition;
import models.Piece;

/**
 * GameEngineImpl implements application-service coordination.
 * 
 * Coordinator responsibilities:
 * - Apply game_over guard
 * - Apply motion_in_progress guard (via RealTimeArbiter.hasActiveMotion)
 * - Delegate validation to RuleEngine
 * - Start validated motions through RealTimeArbiter
 * - Delegate time advancement to RealTimeArbiter
 * - Create GameSnapshot for rendering
 * 
 * Does NOT contain:
 * - Piece movement logic (RuleEngine)
 * - Real-time motion state (RealTimeArbiter)
 * - Rendering
 * - Input parsing
 * - Test-specific logic
 */
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
        // Guard 1: Check game_over
        if (winManager.isGameOver()) {
            return MoveResult.rejected(MoveResult.GAME_OVER);
        }

        // Guard 2: Check motion_in_progress
        if (realTimeArbiter.hasActiveMotion()) {
            return MoveResult.rejected(MoveResult.MOTION_IN_PROGRESS);
        }

        // Delegate to RuleEngine for rule-level validation
        MoveValidation validation = ruleEngine.validateMove(board, srcRow, srcCol, destRow, destCol);

        if (!validation.isValid()) {
            // Return rule-level reason
            return MoveResult.rejected(validation.getReason());
        }

        // Move is valid - start motion through RealTimeArbiter
        Piece piece = board.getPieceAt(srcRow, srcCol);
        long durationMs = calculateMotionDuration(srcRow, srcCol, destRow, destCol);
        realTimeArbiter.startMotion(piece, srcRow, srcCol, destRow, destCol, durationMs);

        return MoveResult.accepted();
    }

    @Override
    public void pause(long durationMs) {
        // Delegate time advancement to RealTimeArbiter
        // This is safe even when game is over (no-op if no active motions)
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
        // Create snapshot with current board state
        return new GameSnapshot(board, winManager.isGameOver());
    }

    /**
     * Calculate motion duration based on distance.
     * Each cell takes 1000 ms.
     */
    private long calculateMotionDuration(int srcRow, int srcCol, int destRow, int destCol) {
        int rowDist = Math.abs(destRow - srcRow);
        int colDist = Math.abs(destCol - srcCol);
        int cells = Math.max(rowDist, colDist);
        if (cells == 0) cells = 1;
        return cells * 1000L;
    }
}
