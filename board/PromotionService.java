package board;

import models.Piece;
import board.BoardContext;
import rules.PromotionRule;

public class PromotionService {
    private final PromotionRule promotionRule;

    public PromotionService(PromotionRule promotionRule) {
        this.promotionRule = promotionRule;
    }

    public Piece applyPromotion(Piece piece, int toRow, BoardContext context) {
        return promotionRule.checkPromotion(piece, toRow, context);
    }
}