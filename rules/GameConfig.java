package rules;

import java.util.HashMap;
import java.util.Map;
import models.PieceType;
import rules.*;

/** Central configuration for board size, timing, and rule strategies. */
public class GameConfig {
    private final int boardWidth;
    private final int boardHeight;
    private final int moveDurationMs;
    private final int pixelsPerCell;
    private final Map<PieceType, MovementRule> movementRules;
    private final PromotionRule promotionRule;
    private final WinCondition winCondition;
    
    public GameConfig(int boardWidth, int boardHeight, int moveDurationMs, int pixelsPerCell,
                      Map<PieceType, MovementRule> movementRules,
                      PromotionRule promotionRule, WinCondition winCondition) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.moveDurationMs = moveDurationMs;
        this.pixelsPerCell = pixelsPerCell;
        this.movementRules = new HashMap<>(movementRules);
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
                                 movementRules, promotionRule, winCondition);
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
