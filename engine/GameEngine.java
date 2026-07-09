package engine;

/**
 * GameEngine interface - application-service coordinator.
 * 
 * GameEngine is the public command boundary. It coordinates:
 * - Board (logical occupancy)
 * - RuleEngine (move validation)
 * - RealTimeArbiter (motion and time)
 * 
 * GameEngine does NOT contain:
 * - Piece-specific movement logic (that's RuleEngine/PieceRules)
 * - Real-time motion state (that's RealTimeArbiter)
 * - Rendering code
 * - Input parsing or pixel mapping
 * - Test-runner logic
 * 
 * Responsibilities:
 * - Check game_over guard
 * - Check motion_in_progress guard (delegate to RealTimeArbiter)
 * - Delegate move validation to RuleEngine
 * - Start legal motions through RealTimeArbiter
 * - Advance time through RealTimeArbiter
 * - Provide read-only GameSnapshot for rendering
 */
public interface GameEngine {
    /**
     * Request a move from source to destination.
     * Returns immediately with MoveResult.
     * 
     * Guards applied in order:
     * 1. Check game_over flag
     * 2. Check if RealTimeArbiter has active motion
     * 3. Call RuleEngine.validateMove
     * 4. If valid, call RealTimeArbiter.startMotion
     * 
     * Rejection reasons:
     * - "ok": accepted and motion started
     * - "game_over": game has ended, no moves allowed
     * - "motion_in_progress": another piece is moving, try later
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
     * Advance simulated time, allowing motions to progress.
     * Delegates to RealTimeArbiter.advanceTime.
     * 
     * Safe to call when game is over (no-op if no active motions).
     * 
     * @param durationMs milliseconds to advance
     */
    void pause(long durationMs);
    
    /**
     * Create a read-only snapshot of current game state.
     * Snapshot contains logical board state and animation state.
     * 
     * @return GameSnapshot with board, piece positions, game-over flag
     */
    GameSnapshot snapshot();
}

