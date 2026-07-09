package engine;

import rules.WinCondition;

/**
 * WinManager tracks game-over state.
 * Delegates to WinCondition strategy.
 * Owned by engine layer.
 */
public class WinManager {
    private final WinCondition winCondition;

    public WinManager(WinCondition winCondition) {
        this.winCondition = winCondition;
    }

    public void recordKingCapture(char winnerColor) {
        winCondition.recordKingCapture(winnerColor);
    }

    public boolean isGameOver() {
        return winCondition.isGameOver(null);
    }

    public Character getWinner() {
        return winCondition.getWinner(null);
    }
}
