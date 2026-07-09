package engine;

import board.BoardContext;

/**
 * GameEngine interface - application-level move orchestration.
 * 
 * Responsibilities:
 * - Accept move requests and return MoveResult with reason string
 * - Guard against game_over and motion_in_progress conditions
 * - Delegate validation to RuleEngine (rule-level reasons)
 * - Manage motion lifecycle (airborne pieces, completion)
 * - Provide read-only snapshot and wait functionality
 */
public interface GameEngine {
    /**
     * Request a move. Returns immediately with MoveResult.
     * 
     * Rejection reasons:
     * - "ok": accepted
     * - "game_over": game has ended
     * - "motion_in_progress": pieces are animating
     * - "outside_board", "empty_source", "friendly_destination", "illegal_piece_move": rule-level
     * 
     * @param srcRow source row
     * @param srcCol source column
     * @param destRow destination row
     * @param destCol destination column
     * @return MoveResult with isAccepted() and getReason()
     */
    MoveResult requestMove(int srcRow, int srcCol, int destRow, int destCol);
    
    /**
     * Pause for duration (ms), allowing motion to progress.
     * Safe to call when game is over.
     * 
     * @param durationMs milliseconds to pause
     */
    void pause(long durationMs);
    
    /**
     * Create a read-only snapshot of current game state.
     * 
     * @return GameSnapshot with board and metadata
     */
    GameSnapshot snapshot();
}

