package board;

import rules.WinCondition;

public class WinManager {
    private final WinCondition winCondition;

    public WinManager(WinCondition winCondition) {
        this.winCondition = winCondition;
    }

    public void recordKingCapture(char winnerColor) {
        // Delegate to underlying strategy
        winCondition.recordKingCapture(winnerColor);
    }

    public boolean isGameOver() {
        // Many WinCondition implementations don't need context; pass null
        return winCondition.isGameOver(null);
    }

    public Character getWinner() {
        return winCondition.getWinner(null);
    }
}