package rules;

import board.Board;
import models.Piece;

/**
 * StandardRuleEngine implements move validation with MoveValidation result.
 * Returns stable reason strings for all cases.
 */
public class StandardRuleEngine implements RuleEngine {
    private final GameConfig config;

    public StandardRuleEngine(GameConfig config) {
        this.config = config;
    }

    @Override
    public MoveValidation validateMove(Board board, int srcRow, int srcCol, int destRow, int destCol) {
        // Check bounds - source
        if (!board.isValid(srcRow, srcCol)) {
            return MoveValidation.invalid(MoveValidation.OUTSIDE_BOARD);
        }

        // Check bounds - destination
        if (!board.isValid(destRow, destCol)) {
            return MoveValidation.invalid(MoveValidation.OUTSIDE_BOARD);
        }

        // Check source is not empty
        Piece source = board.getPieceAt(srcRow, srcCol);
        if (source == null) {
            return MoveValidation.invalid(MoveValidation.EMPTY_SOURCE);
        }

        // Check destination is not friendly
        Piece dest = board.getPieceAt(destRow, destCol);
        if (dest != null && dest.getColor() == source.getColor()) {
            return MoveValidation.invalid(MoveValidation.FRIENDLY_DESTINATION);
        }

        // Delegate piece movement legality to strategy configured for piece type.
        MovementRule movementRule = config.getMovementRule(source.getType());
        if (movementRule == null || !movementRule.isValidMove(source, srcRow, srcCol, destRow, destCol, board)) {
            return MoveValidation.invalid(MoveValidation.ILLEGAL_PIECE_MOVE);
        }

        return MoveValidation.ok();
    }
}
