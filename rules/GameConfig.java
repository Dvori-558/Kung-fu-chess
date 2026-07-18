package rules;

import java.util.HashMap;
import java.util.Map;
import models.PieceType;

/** Central configuration for board size, timing, and rule strategies. */
public class GameConfig {
    private final int boardWidth;
    private final int boardHeight;
    private final int moveDurationMs;
    private final int pixelsPerCell;
    private final Map<PieceType, MovementRule> movementRules;
    private final Map<PieceType, Double> moveSpeedCellsPerSecByType;
    private final PromotionRule promotionRule;
    private final WinCondition winCondition;
    
    public GameConfig(int boardWidth, int boardHeight, int moveDurationMs, int pixelsPerCell,
                      Map<PieceType, MovementRule> movementRules,
                      Map<PieceType, Double> moveSpeedCellsPerSecByType,
                      PromotionRule promotionRule, WinCondition winCondition) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.moveDurationMs = moveDurationMs;
        this.pixelsPerCell = pixelsPerCell;
        this.movementRules = new HashMap<>(movementRules);
        this.moveSpeedCellsPerSecByType = new HashMap<>(moveSpeedCellsPerSecByType);
        this.promotionRule = promotionRule;
        this.winCondition = winCondition;
    }
    
    public int getBoardWidth() {
        return boardWidth;
    }
    
    public int getBoardHeight() {
        return boardHeight;
    }
    
    public int getMoveDurationMs() {
        return moveDurationMs;
    }
    
    public int getPixelsPerCell() {
        return pixelsPerCell;
    }
    
    public MovementRule getMovementRule(PieceType type) {
        return movementRules.get(type);
    }

    /** Optional move speed in cells per second for a specific piece type. */
    public double getMoveSpeedCellsPerSec(PieceType type) {
        if (type == null) return 0.0;
        Double speed = moveSpeedCellsPerSecByType.get(type);
        return speed == null ? 0.0 : speed;
    }
    
    public PromotionRule getPromotionRule() {
        return promotionRule;
    }
    
    public WinCondition getWinCondition() {
        return winCondition;
    }
    
    /** Builder for GameConfig. */
    public static class Builder {
        private int boardWidth = 8;
        private int boardHeight = 8;
        private int moveDurationMs = 1000;
        private int pixelsPerCell = 100;
        private Map<PieceType, MovementRule> movementRules = new HashMap<>();
        private Map<PieceType, Double> moveSpeedCellsPerSecByType = new HashMap<>();
        private PromotionRule promotionRule = new StandardPromotionRule();
        private WinCondition winCondition = new StandardWinCondition();
        
        public Builder boardWidth(int width) {
            this.boardWidth = width;
            return this;
        }
        
        public Builder boardHeight(int height) {
            this.boardHeight = height;
            return this;
        }
        
        public Builder moveDurationMs(int ms) {
            this.moveDurationMs = ms;
            return this;
        }
        
        public Builder pixelsPerCell(int pixels) {
            this.pixelsPerCell = pixels;
            return this;
        }
        
        public Builder addMovementRule(MovementRule rule) {
            this.movementRules.put(rule.getPieceType(), rule);
            return this;
        }

        /** Sets piece movement speed in cells/second for duration calculation. */
        public Builder pieceMoveSpeed(PieceType type, double speedCellsPerSec) {
            if (type == null) {
                throw new IllegalArgumentException("Piece type cannot be null");
            }
            if (speedCellsPerSec <= 0.0) {
                throw new IllegalArgumentException("Speed must be > 0");
            }
            this.moveSpeedCellsPerSecByType.put(type, speedCellsPerSec);
            return this;
        }
        
        public Builder promotionRule(PromotionRule rule) {
            this.promotionRule = rule;
            return this;
        }
        
        public Builder winCondition(WinCondition condition) {
            this.winCondition = condition;
            return this;
        }
        
        public GameConfig build() {
            return new GameConfig(boardWidth, boardHeight, moveDurationMs, pixelsPerCell,
                                 movementRules, moveSpeedCellsPerSecByType, promotionRule, winCondition);
        }
        
        /** Loads standard chess movement rules. */
        public Builder buildStandardChess() {
            this.addMovementRule(new KingMovementRule());
            this.addMovementRule(new QueenMovementRule());
            this.addMovementRule(new RookMovementRule());
            this.addMovementRule(new BishopMovementRule());
            this.addMovementRule(new KnightMovementRule());
            this.addMovementRule(new PawnMovementRule());
            return this;
        }
    }
}
