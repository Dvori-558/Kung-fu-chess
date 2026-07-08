package rules;

import models.Piece;
import board.BoardContext;

public class StandardRuleEngine implements RuleEngine {
    private final GameConfig config;

    public StandardRuleEngine(GameConfig config) {
        this.config = config;
    }

    @Override
    public boolean isValidMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol, BoardContext context) {
        MovementRule rule = config.getMovementRule(piece.getType());
        if (rule == null) return false;
        return rule.isValidMove(piece, fromRow, fromCol, toRow, toCol, context);
    }
}
