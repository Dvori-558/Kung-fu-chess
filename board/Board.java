package board;

import models.Piece;
import models.PieceType;
import rules.GameConfig;

/** Stores logical board state and board-level queries. */
public class Board implements BoardContext {
    private final Piece[][] grid;
    private final int height;
    private final int width;

    private Board(Piece[][] grid) {
        this.grid = grid;
        this.height = grid.length;
        this.width = grid.length > 0 ? grid[0].length : 0;
    }

    public static Board create(Piece[][] grid, GameConfig config) {
        return new Board(grid);
    }

    public boolean isEmpty() {
        return height == 0 || width == 0;
    }

    @Override
    public Piece getPieceAt(int row, int col) {
        if (!isValid(row, col)) return null;
        return grid[row][col];
    }

    @Override
    public boolean isValid(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    @Override
    public boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);
        int r = fromRow + rowStep;
        int c = fromCol + colStep;
        while (r != toRow || c != toCol) {
            if (getPieceAt(r, c) != null) return false;
            r += rowStep;
            c += colStep;
        }
        return true;
    }

    @Override
    public int getHeight() { return height; }

    @Override
    public int getWidth() { return width; }

    /** Updates one board cell. */
    public void setPieceAt(int row, int col, Piece piece) {
        if (isValid(row, col)) {
            grid[row][col] = piece;
        }
    }
}
