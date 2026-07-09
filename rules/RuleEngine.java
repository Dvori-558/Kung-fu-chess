package rules;

import board.Board;

/**
 * RuleEngine interface - rule-level move validation.
 * 
 * Responsibilities:
 * - Validate moves according to piece movement rules
 * - Check board boundaries and path clarity
 * - Return MoveValidation with stable reason strings
 * - Do not mutate the board
 * 
 * Stable reason constants:
 * - "ok": move is valid
 * - "outside_board": source or destination out of bounds
 * - "empty_source": no piece at source
 * - "friendly_destination": destination has own piece
 * - "illegal_piece_move": move violates piece rules or path blocked
 */
public interface RuleEngine {
    /**
     * Validate a move at the rule level.
     * Does not check game state (game_over, motion_in_progress).
     * Does not mutate the board.
     * 
     * @param board the game board
     * @param srcRow source row
     * @param srcCol source column
     * @param destRow destination row
     * @param destCol destination column
     * @return MoveValidation with isValid() and getReason()
     */
    MoveValidation validateMove(Board board, int srcRow, int srcCol, int destRow, int destCol);
}
