package rules;

import models.Piece;
import models.PieceType;
import board.BoardContext;

/**
 * MovementRule defines how a piece type can move.
 * 
 * Design Pattern: Strategy Pattern
 * Purpose: Encapsulate move validation logic for each piece type
 * 
 * Benefits:
 * - Each rule is independent and testable
 * - Can add new piece types without modifying existing code
 * - Supports custom movement rules for custom games
 */
public interface MovementRule {
    /**
     * Check if a move is valid for this piece type.
     * 
     * @param piece The piece being moved
     * @param fromRow Source row
     * @param fromCol Source column
     * @param toRow Destination row
     * @param toCol Destination column
     * @param context Provides board state queries (path clear, target piece, etc.)
     * @return true if the move is valid
     */
    boolean isValidMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol, 
                        BoardContext context);
    
    /**
     * Get the piece type this rule applies to.
     */
    PieceType getPieceType();
}
