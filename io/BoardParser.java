package io;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import models.Piece;
import models.PieceType;
import board.Board;
import rules.GameConfig;

/**
 * BoardParser reads textual board representation and creates a Board model.
 * 
 * Textual format:
 *   - Each row is on a separate line
 *   - Cells are separated by spaces
 *   - "." means empty cell
 *   - Piece format: color + type, e.g., "wK" (white king), "bP" (black pawn)
 * 
 * Piece types: K (king), Q (queen), R (rook), B (bishop), N (knight), P (pawn)
 * Colors: w (white), b (black)
 * 
 * This is a read-only adapter. It does not know about game logic, rules, or rendering.
 */
public class BoardParser {
    
    /**
     * Parse textual board input and create a Board.
     * 
     * Input format:
     *   Board
     *   wK . .
     *   . wR .
     *   . . bK
     * 
     * @param scanner input source
     * @param config game configuration (for board dimensions)
     * @return parsed Board, or empty board if no valid input
     * @throws IllegalArgumentException if board structure is invalid
     */
    public static Board parse(Scanner scanner, GameConfig config) {
        List<String> boardRows = new ArrayList<>();
        boolean boardStarted = false;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String trimmed = line.trim();

            // Skip empty lines before board starts
            if (!boardStarted) {
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (trimmed.equalsIgnoreCase("Board") || trimmed.equalsIgnoreCase("Board:")) {
                    boardStarted = true;
                    continue;
                }
                // Treat any non-empty, non-"Board" line as start of board data
                boardStarted = true;
                boardRows.add(trimmed);
                continue;
            }

            // Stop when we hit "Commands"
            if (trimmed.equalsIgnoreCase("Commands") || trimmed.equalsIgnoreCase("Commands:")) {
                break;
            }

            // Skip empty lines within board
            if (trimmed.isEmpty()) {
                continue;
            }

            boardRows.add(trimmed);
        }

        // Empty board case
        if (boardRows.isEmpty()) {
            return Board.create(new Piece[0][0], config);
        }

        // Parse all rows into a grid
        return parseGrid(boardRows, config);
    }

    /**
     * Parse grid rows into a Board.
     */
    private static Board parseGrid(List<String> rows, GameConfig config) {
        if (rows.isEmpty()) {
            return Board.create(new Piece[0][0], config);
        }

        // Determine column count from first row
        String[] firstRowTokens = rows.get(0).split("\\s+");
        int cols = firstRowTokens.length;

        // Create grid
        Piece[][] grid = new Piece[rows.size()][cols];

        // Parse each row
        for (int i = 0; i < rows.size(); i++) {
            String[] tokens = rows.get(i).split("\\s+");

            if (tokens.length != cols) {
                throw new IllegalArgumentException(
                    "Row " + i + " has " + tokens.length + " columns, expected " + cols);
            }

            for (int j = 0; j < cols; j++) {
                grid[i][j] = parsePiece(tokens[j]);
            }
        }

        return Board.create(grid, config);
    }

    /**
     * Parse a single piece token.
     * 
     * @param token piece string: "wK", "bP", ".", etc.
     * @return Piece object, or null for empty cell (".")
     * @throws IllegalArgumentException if token format is invalid
     */
    private static Piece parsePiece(String token) {
        token = token.trim();

        if (token.equals(".")) {
            return null;
        }

        if (token.length() != 2) {
            throw new IllegalArgumentException(
                "Invalid piece format: '" + token + "' (expected 2 characters)");
        }

        char colorChar = token.charAt(0);
        char typeChar = token.charAt(1);

        // Parse color
        char color;
        if (colorChar == 'w') {
            color = Piece.WHITE;
        } else if (colorChar == 'b') {
            color = Piece.BLACK;
        } else {
            throw new IllegalArgumentException(
                "Invalid color: '" + colorChar + "' (expected 'w' or 'b')");
        }

        // Parse piece type
        PieceType type;
        if (typeChar == 'K') type = PieceType.KING;
        else if (typeChar == 'Q') type = PieceType.QUEEN;
        else if (typeChar == 'R') type = PieceType.ROOK;
        else if (typeChar == 'B') type = PieceType.BISHOP;
        else if (typeChar == 'N') type = PieceType.KNIGHT;
        else if (typeChar == 'P') type = PieceType.PAWN;
        else throw new IllegalArgumentException(
            "Invalid piece type: '" + typeChar + "' (expected K/Q/R/B/N/P)");

        return new Piece(color, type);
    }
}
