package engine;

/** Command API for game actions and state queries. */
public interface GameEngine {
    /** Validates and starts a move request. */
    MoveResult requestMove(int srcRow, int srcCol, int destRow, int destCol);

    /**
     * Starts an airborne jump from a board cell.
     * Returns true if jump started.
     */
    boolean requestJump(int row, int col);
    
    /** Advances simulated time in milliseconds. */
    void pause(long durationMs);

    /** @return true when at least one motion is active. */
    boolean hasActiveMotion();

    /** @return true when the game is over. */
    boolean isGameOver();
    
    /** Returns a snapshot for rendering/output. */
    GameSnapshot snapshot();
}

