package rules;

import models.Piece;
import models.PieceType;
import board.BoardContext;

/** Strategy interface for piece-specific movement rules. */
public interface MovementRule {
    /** Returns true when the move is legal for this piece. */
    boolean isValidMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol, 
                        BoardContext context);
    
    /** Returns the piece type this rule handles. */
    PieceType getPieceType();
}
