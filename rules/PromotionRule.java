package rules;

import models.Piece;
import board.BoardContext;

/** Strategy interface for piece promotion on arrival. */
public interface PromotionRule {
    /** Returns promoted piece instance for the target row. */
    Piece checkPromotion(Piece piece, int toRow, BoardContext context);
}
