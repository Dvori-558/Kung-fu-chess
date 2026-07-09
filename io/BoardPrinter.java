package io;

import board.Board;
import models.Piece;

/**
 * BoardPrinter outputs the logical board state in textual format.
 * 
 * Output format:
 *   wK . .
 *   . wR .
 *   . . bK
 * 
 * This prints logical occupancy only, not animation state or pixel positions.
 * It is a read-only adapter. It does not know about game logic, rules, or rendering.
 */
public class BoardPrinter {
    
    /**
     * Print the board to standard output.
     * 
     * @param board the board to print
     */
    public static void print(Board board) {
        String output = toString(board);
        System.out.print(output);
    }
    
    /**
     * Convert board to textual representation.
     * 
     * @param board the board to convert
     * @return textual representation with newlines
     */
    public static String toString(Board board) {
        if (board.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < board.getHeight(); row++) {
            if (row > 0) {
                sb.append("\n");
            }

            for (int col = 0; col < board.getWidth(); col++) {
                if (col > 0) {
                    sb.append(" ");
                }

                Piece piece = board.getPieceAt(row, col);
                if (piece == null) {
                    sb.append(".");
                } else {
                    sb.append(piece.toString());
                }
            }
        }

        sb.append("\n");
        return sb.toString();
    }
}
