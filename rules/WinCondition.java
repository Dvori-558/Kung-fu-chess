package rules;

import board.BoardContext;

/**
 * WinCondition defines the game-over condition.
 * 
 * Design Pattern: Strategy Pattern
 * Purpose: Support different win conditions for different games
 */
public interface WinCondition {
    /**
     * Check if the game is over.
     * 
     * @param context Board context
     * @return true if game is over
     */
    boolean isGameOver(BoardContext context);
    
    /**
     * Get the winner's color, or null if not applicable.
     */
    Character getWinner(BoardContext context);

    /**
     * Record that a king was captured and who won.
     */
    void recordKingCapture(char winnerColor);
}
