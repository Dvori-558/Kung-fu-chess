package rules;

import board.Board;
import models.Piece;
import models.PieceType;

/**
 * StandardRuleEngine implements move validation with MoveValidation result.
 * Returns stable reason strings for all cases.
 */
public class StandardRuleEngine implements RuleEngine {
    private final GameConfig config;

    public StandardRuleEngine(GameConfig config) {
        this.config = config;
    }

    @Override
    public MoveValidation validateMove(Board board, int srcRow, int srcCol, int destRow, int destCol) {
        // Check bounds - source
        if (!board.isValid(srcRow, srcCol)) {
            return MoveValidation.invalid(MoveValidation.OUTSIDE_BOARD);
        }

        // Check bounds - destination
        if (!board.isValid(destRow, destCol)) {
            return MoveValidation.invalid(MoveValidation.OUTSIDE_BOARD);
        }

        // Check source is not empty
        Piece source = board.getPieceAt(srcRow, srcCol);
        if (source == null) {
            return MoveValidation.invalid(MoveValidation.EMPTY_SOURCE);
        }

        // Check destination is not friendly
        Piece dest = board.getPieceAt(destRow, destCol);
        if (dest != null && dest.getColor() == source.getColor()) {
            return MoveValidation.invalid(MoveValidation.FRIENDLY_DESTINATION);
        }

        // Check piece-specific movement rules
        if (!isPieceMovementValid(source, srcRow, srcCol, destRow, destCol, board)) {
            return MoveValidation.invalid(MoveValidation.ILLEGAL_PIECE_MOVE);
        }

        return MoveValidation.ok();
    }

    /**
     * Check if piece movement follows piece-specific rules.
     * Also checks path clarity for sliding pieces.
     */
    private boolean isPieceMovementValid(Piece piece, int srcRow, int srcCol, int destRow, int destCol, Board board) {
        PieceType type = piece.getType();

        if (type == PieceType.PAWN) {
            return isValidPawnMove(piece, srcRow, srcCol, destRow, destCol, board);
        } else if (type == PieceType.ROOK) {
            return isValidRookMove(srcRow, srcCol, destRow, destCol, board);
        } else if (type == PieceType.BISHOP) {
            return isValidBishopMove(srcRow, srcCol, destRow, destCol, board);
        } else if (type == PieceType.QUEEN) {
            return isValidQueenMove(srcRow, srcCol, destRow, destCol, board);
        } else if (type == PieceType.KING) {
            return isValidKingMove(srcRow, srcCol, destRow, destCol);
        } else if (type == PieceType.KNIGHT) {
            return isValidKnightMove(srcRow, srcCol, destRow, destCol);
        }
        return false;
    }

    private boolean isValidPawnMove(Piece pawn, int srcRow, int srcCol, int destRow, int destCol, Board board) {
        int rowDiff = destRow - srcRow;
        int colDiff = Math.abs(destCol - srcCol);

        // Direction depends on color
        int direction = (pawn.getColor() == Piece.WHITE) ? 1 : -1;

        // Forward one square
        if (colDiff == 0 && rowDiff == direction) {
            Piece target = board.getPieceAt(destRow, destCol);
            return target == null; // Must be empty
        }

        // Capture diagonally
        if (colDiff == 1 && rowDiff == direction) {
            Piece target = board.getPieceAt(destRow, destCol);
            return target != null && target.getColor() != pawn.getColor();
        }

        return false;
    }

    private boolean isValidRookMove(int srcRow, int srcCol, int destRow, int destCol, Board board) {
        // Rook moves horizontally or vertically
        if (srcRow != destRow && srcCol != destCol) {
            return false; // Not horizontal or vertical
        }

        // Check path is clear
        return board.isPathClear(srcRow, srcCol, destRow, destCol);
    }

    private boolean isValidBishopMove(int srcRow, int srcCol, int destRow, int destCol, Board board) {
        // Bishop moves diagonally
        int rowDiff = Math.abs(destRow - srcRow);
        int colDiff = Math.abs(destCol - srcCol);

        if (rowDiff != colDiff) {
            return false; // Not diagonal
        }

        // Check path is clear
        return board.isPathClear(srcRow, srcCol, destRow, destCol);
    }

    private boolean isValidQueenMove(int srcRow, int srcCol, int destRow, int destCol, Board board) {
        // Queen moves like rook or bishop
        if (srcRow == destRow || srcCol == destCol) {
            // Rook-like
            return board.isPathClear(srcRow, srcCol, destRow, destCol);
        }

        int rowDiff = Math.abs(destRow - srcRow);
        int colDiff = Math.abs(destCol - srcCol);

        if (rowDiff != colDiff) {
            return false; // Not diagonal
        }

        // Bishop-like, check path is clear
        return board.isPathClear(srcRow, srcCol, destRow, destCol);
    }

    private boolean isValidKingMove(int srcRow, int srcCol, int destRow, int destCol) {
        // King moves 1 square in any direction
        int rowDiff = Math.abs(destRow - srcRow);
        int colDiff = Math.abs(destCol - srcCol);

        return rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff > 0);
    }

    private boolean isValidKnightMove(int srcRow, int srcCol, int destRow, int destCol) {
        // Knight moves in L-shape: 2 in one direction, 1 in perpendicular
        int rowDiff = Math.abs(destRow - srcRow);
        int colDiff = Math.abs(destCol - srcCol);

        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }
}
