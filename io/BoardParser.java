package io;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import models.Piece;
import models.PieceType;
import board.Board;
import rules.GameConfig;

/** Parses textual board input into a Board instance. */
public class BoardParser {
    
    /** Reads board rows and stops at Commands. */
    public static Board parse(Scanner scanner, GameConfig config) {
        List<String> boardRows = new ArrayList<>();
        boolean boardStarted = false;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String trimmed = line.trim();

            if (!boardStarted) {
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (trimmed.equalsIgnoreCase("Board") || trimmed.equalsIgnoreCase("Board:")) {
                    boardStarted = true;
                    continue;
                }
                boardStarted = true;
                boardRows.add(trimmed);
                continue;
            }

            if (trimmed.equalsIgnoreCase("Commands") || trimmed.equalsIgnoreCase("Commands:")) {
                break;
            }

            if (trimmed.isEmpty()) {
                continue;
            }

            boardRows.add(trimmed);
        }

        if (boardRows.isEmpty()) {
            return Board.create(new Piece[0][0], config);
        }

        try {
            return parseGrid(boardRows, config);
        } catch (IllegalArgumentException ex) {
            String message = ex.getMessage();
            if (message != null && message.startsWith("UNKNOWN_TOKEN")) {
                System.out.println("ERROR UNKNOWN_TOKEN");
            } else if (message != null && message.startsWith("ROW_WIDTH_MISMATCH")) {
                System.out.println("ERROR ROW_WIDTH_MISMATCH");
            } else {
                System.out.println("ERROR UNKNOWN_TOKEN");
            }
            return Board.create(new Piece[0][0], config);
        }
    }

    /** Parses rectangular rows into a piece grid. */
    private static Board parseGrid(List<String> rows, GameConfig config) {
        if (rows.isEmpty()) {
            return Board.create(new Piece[0][0], config);
        }

        String[] firstRowTokens = rows.get(0).split("\\s+");
        int cols = firstRowTokens.length;

        Piece[][] grid = new Piece[rows.size()][cols];

        for (int i = 0; i < rows.size(); i++) {
            String[] tokens = rows.get(i).split("\\s+");

            if (tokens.length != cols) {
                throw new IllegalArgumentException("ROW_WIDTH_MISMATCH");
            }

            for (int j = 0; j < cols; j++) {
                grid[i][j] = parsePiece(tokens[j]);
            }
        }

        return Board.create(grid, config);
    }

    /** Parses one token into a piece or empty cell. */
    private static Piece parsePiece(String token) {
        token = token.trim();

        if (token.equals(".")) {
            return null;
        }

        if (token.length() != 2) {
            throw new IllegalArgumentException("UNKNOWN_TOKEN");
        }

        char colorChar = token.charAt(0);
        char typeChar = token.charAt(1);

        char color;
        if (colorChar == 'w') {
            color = Piece.WHITE;
        } else if (colorChar == 'b') {
            color = Piece.BLACK;
        } else {
            throw new IllegalArgumentException("UNKNOWN_TOKEN");
        }

        PieceType type;
        if (typeChar == 'K') type = PieceType.KING;
        else if (typeChar == 'Q') type = PieceType.QUEEN;
        else if (typeChar == 'R') type = PieceType.ROOK;
        else if (typeChar == 'B') type = PieceType.BISHOP;
        else if (typeChar == 'N') type = PieceType.KNIGHT;
        else if (typeChar == 'P') type = PieceType.PAWN;
        else throw new IllegalArgumentException("UNKNOWN_TOKEN");

        return new Piece(color, type);
    }
}
