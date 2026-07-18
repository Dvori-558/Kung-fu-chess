package ui;

import java.util.Objects;

/** Immutable board cell coordinate. */
public class BoardCell {
    private final int row;
    private final int col;

    public BoardCell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardCell)) return false;
        BoardCell boardCell = (BoardCell) o;
        return row == boardCell.row && col == boardCell.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}