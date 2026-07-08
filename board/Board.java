package board;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import models.*;
import rules.*;
import utils.CoordinateConverter;

/**
 * Refactored Board class using Strategy pattern and configuration.
 * 
 * Design Principles Applied:
 * - DRY: No duplicate logic
 * - SRP: Board manages state + delegates rules to strategies
 * - Encapsulation: Internal representation hidden, API is clean
 * - No hard-coded values: All constants in GameConfig
 */
public class Board implements BoardContext {
    private Piece[][] grid;
    private int height;
    private int width;
    private GameConfig config;

    private Board(Piece[][] grid, GameConfig config) {
        this.grid = grid;
        this.height = grid.length;
        this.width = grid.length > 0 ? grid[0].length : 0;
        this.config = config;
    }

    public static Board create(Piece[][] grid, GameConfig config) {
        return new Board(grid, config);
    }

    /**
     * Read board from input (text format).
     * Converts string tokens like "wK", "bP" to Piece objects.
     */
    public static Board readFrom(Scanner sc, GameConfig config) {
        List<String> boardRows = new ArrayList<>();
        boolean boardStarted = false;

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String trimmed = line.trim();

            if (!boardStarted) {
                if (trimmed.equals("Board:")) {
                    boardStarted = true;
                    continue;
                }
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (trimmed.equals("Commands:")) {
                    break;
                }
                boardStarted = true;
                boardRows.add(trimmed);
                continue;
            }

            if (trimmed.equals("Commands:")) {
                break;
            }
            if (trimmed.isEmpty()) {
                continue;
            }
            boardRows.add(trimmed);
        }

        if (boardRows.isEmpty()) {
            return new Board(new Piece[0][0], config);
        }

        String[] firstRow = boardRows.get(0).split("\\s+");
        int cols = firstRow.length;
        Piece[][] grid = new Piece[boardRows.size()][cols];

        for (int i = 0; i < boardRows.size(); i++) {
            String[] tokens = boardRows.get(i).split("\\s+");
            if (tokens.length != cols) {
                throw new IllegalArgumentException("Row " + i + " has " + tokens.length + 
                                                   " columns, expected " + cols);
            }
            for (int j = 0; j < cols; j++) {
                grid[i][j] = parsePiece(tokens[j]);
            }
        }

        return new Board(grid, config);
    }

    /**
     * Parse a piece from text format: "wK", "bP", ".", etc.
     */
    private static Piece parsePiece(String token) {
        token = token.trim();  // Remove any leading/trailing whitespace

        if (token.equals(".")) {
            return null;
        }

        if (token.length() != 2) {
            throw new IllegalArgumentException("Invalid piece format: '" + token + 
                                             "' (length=" + token.length() + ")");
        }

        char color = token.charAt(0);
        char typeChar = token.charAt(1);

        PieceType type;
        if (typeChar == 'K') type = PieceType.KING;
        else if (typeChar == 'Q') type = PieceType.QUEEN;
        else if (typeChar == 'R') type = PieceType.ROOK;
        else if (typeChar == 'B') type = PieceType.BISHOP;
        else if (typeChar == 'N') type = PieceType.KNIGHT;
        else if (typeChar == 'P') type = PieceType.PAWN;
        else throw new IllegalArgumentException("Unknown piece type: " + typeChar);

        return new Piece(color, type);
    }

    public boolean isEmpty() {
        return height == 0 || width == 0;
    }

    public boolean isValid() {
        if (isEmpty()) {
            return false;
        }

        for (int i = 0; i < height; i++) {
            if (grid[i] == null || grid[i].length != width) {
                System.out.println("ERROR ROW_WIDTH_MISMATCH");
                return false;
            }
        }
        // All rows are consistent, board is valid
        return true;
    }

    // Board is a pure model now. Game input handling is done by GameEngine.

    // ========== BoardContext Implementation ==========
    @Override
    public Piece getPieceAt(int row, int col) {
        if (!isValid(row, col)) {
            return null;
        }
        return grid[row][col];
    }

    @Override
    public boolean isValid(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    @Override
    public boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);
        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;

        while (currentRow != toRow || currentCol != toCol) {
            if (getPieceAt(currentRow, currentCol) != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        return true;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public Piece getAirbornePieceAt(int row, int col) {
        // Board as model does not track airborne state; engine/managers handle it
        return null;
    }

    // Package helper for managers/engine
    void setPieceAt(int row, int col, Piece piece) {
        if (isValid(row, col)) {
            grid[row][col] = piece;
        }
    }


    public void printBoard() {
        for (int i = 0; i < height; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < width; j++) {
                if (j > 0) {
                    sb.append(" ");
                }
                Piece piece = grid[i][j];
                sb.append(piece == null ? "." : piece.toString());
            }
            System.out.println(sb.toString());
        }
    }

    // Remove UI handling from Board. Board is a pure model now.
}

