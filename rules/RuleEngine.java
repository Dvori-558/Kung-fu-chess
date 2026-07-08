package rules;

import models.Piece;
import board.BoardContext;

public interface RuleEngine {
    boolean isValidMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol, BoardContext context);
}
