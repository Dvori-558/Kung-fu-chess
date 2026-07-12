package rules;

import board.Board;

/** Validates move legality without mutating the board. */
public interface RuleEngine {
    /** Returns validation result and reason for a move request. */
    MoveValidation validateMove(Board board, int srcRow, int srcCol, int destRow, int destCol);
}
