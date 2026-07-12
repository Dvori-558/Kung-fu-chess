package rules;

import models.Piece;
import models.PieceType;
import board.BoardContext;

/**
 * Standard movement rules for all chess piece types.
 * 
 * Design: Each piece type is a separate class implementing MovementRule.
 * All in one file because they're cohesive - they all validate piece moves.
 * Each class remains independent and testable.
 */

class KingMovementRule implements MovementRule {
    @Override
    public boolean isValidMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol,
                               BoardContext context) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        
        if (rowDiff > 1 || colDiff > 1 || (rowDiff == 0 && colDiff == 0)) {
            return false;
        }
        
        Piece target = context.getPieceAt(toRow, toCol);
        return target == null || piece.isOppositeColor(target);
    }
    
    @Override
    public PieceType getPieceType() {
        return PieceType.KING;
    }
}

class RookMovementRule implements MovementRule {
    @Override
    public boolean isValidMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol,
                               BoardContext context) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        
        if ((rowDiff == 0 && colDiff == 0) || (rowDiff != 0 && colDiff != 0)) {
            return false;
        }
        
        if (!context.isPathClear(fromRow, fromCol, toRow, toCol)) {
            return false;
        }
        
        Piece target = context.getPieceAt(toRow, toCol);
        return target == null || piece.isOppositeColor(target);
    }
    
    @Override
    public PieceType getPieceType() {
        return PieceType.ROOK;
    }
}

class BishopMovementRule implements MovementRule {
    @Override
    public boolean isValidMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol,
                               BoardContext context) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        
        if (rowDiff != colDiff || rowDiff == 0) {
            return false;
        }
        
        if (!context.isPathClear(fromRow, fromCol, toRow, toCol)) {
            return false;
        }
        
        Piece target = context.getPieceAt(toRow, toCol);
        return target == null || piece.isOppositeColor(target);
    }
    
    @Override
    public PieceType getPieceType() {
        return PieceType.BISHOP;
    }
}

class QueenMovementRule implements MovementRule {
    private final RookMovementRule rookRule = new RookMovementRule();
    private final BishopMovementRule bishopRule = new BishopMovementRule();
    
    @Override
    public boolean isValidMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol,
                               BoardContext context) {
        return rookRule.isValidMove(piece, fromRow, fromCol, toRow, toCol, context) ||
               bishopRule.isValidMove(piece, fromRow, fromCol, toRow, toCol, context);
    }
    
    @Override
    public PieceType getPieceType() {
        return PieceType.QUEEN;
    }
}

class KnightMovementRule implements MovementRule {
    @Override
    public boolean isValidMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol,
                               BoardContext context) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        
        if (!((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2))) {
            return false;
        }
        
        Piece target = context.getPieceAt(toRow, toCol);
        return target == null || piece.isOppositeColor(target);
    }
    
    @Override
    public PieceType getPieceType() {
        return PieceType.KNIGHT;
    }
}

class PawnMovementRule implements MovementRule {
    @Override
    public boolean isValidMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol,
                               BoardContext context) {
        int rowDiff = toRow - fromRow;
        int colDiff = Math.abs(toCol - fromCol);
        int direction = piece.getColor() == Piece.WHITE ? -1 : 1;
        
        // Single step forward
        if (colDiff == 0 && rowDiff == direction && context.getPieceAt(toRow, toCol) == null) {
            return true;
        }
        
        // Two steps from starting row
        int startRow = piece.getColor() == Piece.WHITE ? context.getHeight() - 2 : 1;
        if (colDiff == 0 && rowDiff == 2 * direction && fromRow == startRow &&
            context.getPieceAt(toRow, toCol) == null &&
            context.isPathClear(fromRow, fromCol, toRow, toCol)) {
            return true;
        }
        
        // Diagonal capture
        if (colDiff == 1 && rowDiff == direction) {
            Piece target = context.getPieceAt(toRow, toCol);
            return target != null && piece.isOppositeColor(target);
        }
        
        return false;
    }
    
    @Override
    public PieceType getPieceType() {
        return PieceType.PAWN;
    }
}
