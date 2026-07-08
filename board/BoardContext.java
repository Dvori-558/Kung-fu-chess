package board;

import models.Piece;

/**
 * BoardContext provides board state queries for movement validation.
 * 
 * Design Pattern: Context/Query Object
 * Purpose: Isolate movement rules from board implementation details
 * 
 * Benefits:
 * - Movement rules don't depend on Board internals (Encapsulation)
 * - Can mock for unit testing
 * - Can swap board representation without changing rules
 */
public interface BoardContext {
    /**
     * Get piece at given position, or null if empty.
     */
    Piece getPieceAt(int row, int col);
    
    /**
     * Check if position is within board bounds.
     */
    boolean isValid(int row, int col);
    
    /**
     * Check if path from (fromRow, fromCol) to (toRow, toCol) is clear.
     * Excludes start and end positions.
     */
    boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol);
    
    /**
     * Get board height.
     */
    int getHeight();
    
    /**
     * Get board width.
     */
    int getWidth();
    
    /**
     * Check if there's an airborne piece at position.
     */
    Piece getAirbornePieceAt(int row, int col);
}
