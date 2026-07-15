package ui;

import java.util.List;

/** Immutable snapshot for renderer consumption only. */
public class VisualSnapshot {
    private final int width;
    private final int height;
    private final int selectedRow;
    private final int selectedCol;
    private final boolean gameOver;
    private final List<PieceVisualSnapshot> pieces;

    public VisualSnapshot(int width, int height, int selectedRow, int selectedCol, boolean gameOver, List<PieceVisualSnapshot> pieces) {
        this.width = width;
        this.height = height;
        this.selectedRow = selectedRow;
        this.selectedCol = selectedCol;
        this.gameOver = gameOver;
        this.pieces = pieces;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getSelectedRow() { return selectedRow; }
    public int getSelectedCol() { return selectedCol; }
    public boolean isGameOver() { return gameOver; }
    public List<PieceVisualSnapshot> getPieces() { return pieces; }
}