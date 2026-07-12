package rules;

import board.BoardContext;

/** Strategy interface for game-over and winner logic. */
public interface WinCondition {
    /** Returns true when the game has ended. */
    boolean isGameOver(BoardContext context);
    
    /** Returns winner color or null. */
    Character getWinner(BoardContext context);

    /** Records winner after king capture. */
    void recordKingCapture(char winnerColor);
}
