package engine;

import board.Board;

/**
 * GameSnapshot is a read-only snapshot of game state.
 * Contains board and metadata at a moment in time.
 */
public class GameSnapshot {
    private final Board board;
    private final boolean gameOver;

    public GameSnapshot(Board board, boolean gameOver) {
        this.board = board;
        this.gameOver = gameOver;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
