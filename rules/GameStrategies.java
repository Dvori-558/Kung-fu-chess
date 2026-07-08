package rules;

import models.Piece;
import models.PieceType;
import board.BoardContext;

/**
 * Game strategy implementations for promotion and win conditions.
 * 
 * Design: Both strategies are small and cohesive.
 * Kept together because they both affect game flow/ending.
 * Each class remains independent and pluggable.
 */

/**
 * Standard chess promotion: pawn reaches end → becomes queen.
 */
class StandardPromotionRule implements PromotionRule {
    @Override
    public Piece checkPromotion(Piece piece, int toRow, BoardContext context) {
        if (!piece.getType().equals(PieceType.PAWN)) {
            return piece;
        }
        
        boolean reachedEnd = (piece.getColor() == Piece.WHITE && toRow == 0) ||
                            (piece.getColor() == Piece.BLACK && toRow == context.getHeight() - 1);
        
        if (reachedEnd) {
            return new Piece(piece.getColor(), PieceType.QUEEN);
        }
        return piece;
    }
}

/**
 * Standard chess win condition: game ends when a king is captured.
 */
class StandardWinCondition implements WinCondition {
    private Character winner = null;
    
    /**
     * Called when a king is captured. Records the winner.
     */
    public void recordKingCapture(char winnerColor) {
        this.winner = winnerColor;
    }
    
    @Override
    public boolean isGameOver(BoardContext context) {
        return winner != null;
    }
    
    @Override
    public Character getWinner(BoardContext context) {
        return winner;
    }
}
