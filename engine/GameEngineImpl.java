package engine;

import board.*;
import models.Piece;
import rules.*;

/**
 * GameEngineImpl implements application-level move orchestration.
 * 
 * Responsibilities:
 * - Guard against game_over and motion_in_progress
 * - Delegate validation to RuleEngine
 * - Return MoveResult with stable reason strings
 * - Manage motion lifecycle (call moveManager.startMove)
 * - Provide read-only snapshot
 */
public class GameEngineImpl implements GameEngine {
    private final Board board;
    private final GameConfig config;
    private final RuleEngine ruleEngine;
    private final MoveManager moveManager;
    private final WinManager winManager;
    private final TimerService timerService;

    public GameEngineImpl(Board board, GameConfig config) {
        this.board = board;
        this.config = config;
        this.ruleEngine = new StandardRuleEngine(config);
        this.winManager = new WinManager(config.getWinCondition());
        
        PromotionService promotionService = new PromotionService(config.getPromotionRule());
        AirborneManager airborneManager = new AirborneManager(board, config);
        this.moveManager = new MoveManager(board, config, promotionService, winManager, airborneManager);
        this.timerService = new TimerService(moveManager, airborneManager);
    }

    @Override
    public MoveResult requestMove(int srcRow, int srcCol, int destRow, int destCol) {
        // Guard 1: game_over
        if (winManager.isGameOver()) {
            return MoveResult.rejected(MoveResult.GAME_OVER);
        }

        // Guard 2: motion_in_progress
        if (moveManager.isMovePending()) {
            return MoveResult.rejected(MoveResult.MOTION_IN_PROGRESS);
        }

        // Delegate to RuleEngine for rule-level validation
        MoveValidation validation = ruleEngine.validateMove(board, srcRow, srcCol, destRow, destCol);

        if (!validation.isValid()) {
            // Return rule-level reason
            return MoveResult.rejected(validation.getReason());
        }

        // Move is valid - start motion
        Piece piece = board.getPieceAt(srcRow, srcCol);
        moveManager.startMove(piece, srcRow, srcCol, destRow, destCol);

        return MoveResult.accepted();
    }

    @Override
    public void pause(long durationMs) {
        if (!winManager.isGameOver()) {
            timerService.tick((int) durationMs);
        }
    }

    @Override
    public GameSnapshot snapshot() {
        return new GameSnapshot(board, winManager.isGameOver());
    }
}
