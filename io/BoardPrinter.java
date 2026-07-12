package io;

import board.Board;
import models.Piece;

/** Renders logical board state as plain text. */
public class BoardPrinter {
    
    /** Prints board text to stdout. */
    public static void print(Board board) {
        String output = toString(board);
        System.out.print(output);
    }
    
    /** Converts board state to multiline text. */
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
