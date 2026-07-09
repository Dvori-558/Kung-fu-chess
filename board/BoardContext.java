package board;

import models.Piece;

/**
 * BoardContext provides board state queries.
 * Used by RuleEngine and movement rules to inspect the board without owning it.
 */
public interface BoardContext {
    Piece getPieceAt(int row, int col);
    boolean isValid(int row, int col);
    boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol);
    int getHeight();
    int getWidth();
}
