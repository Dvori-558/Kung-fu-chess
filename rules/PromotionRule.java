package rules;

import models.Piece;
import board.BoardContext;

/**
 * PromotionRule defines what happens when a pawn reaches the end of the board.
 * 
 * Design Pattern: Strategy Pattern
 * Purpose: Support different promotion rules for different games
 */
public interface PromotionRule {
    /**
     * Check if a piece should be promoted.
     * 
     * @param piece The piece that moved
     * @param toRow The destination row
     * @param context Board context for queries
     * @return The promoted piece, or null if no promotion
     */
    Piece checkPromotion(Piece piece, int toRow, BoardContext context);
}
