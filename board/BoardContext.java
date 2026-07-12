package board;

import models.Piece;

/** Read-only board query interface used by rule components. */
public interface BoardContext {
    Piece getPieceAt(int row, int col);
    boolean isValid(int row, int col);
    boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol);
    int getHeight();
    int getWidth();
}
