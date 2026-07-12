package engine;

/** Command API for game actions and state queries. */
public interface GameEngine {
    /** Validates and starts a move request. */
    MoveResult requestMove(int srcRow, int srcCol, int destRow, int destCol);
    
    /** Advances simulated time in milliseconds. */
    void pause(long durationMs);

    /** @return true when at least one motion is active. */
    boolean hasActiveMotion();

    /** @return true when the game is over. */
    boolean isGameOver();
    
    /** Returns a snapshot for rendering/output. */
    GameSnapshot snapshot();
}

