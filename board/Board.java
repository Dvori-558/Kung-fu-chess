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
    
    // Selection state
    private int selectedRow = -1;
    private int selectedCol = -1;
    
    // Movement state
    private boolean movePending = false;
    private Piece pendingPiece;
    private int pendingFromRow;
    private int pendingFromCol;
    private int pendingToRow;
    private int pendingToCol;
    private int pendingTimeRemaining;
    
    // Airborne piece (from jump)
    private Piece airbornePiece = null;
    private int airbornePieceRow = -1;
    private int airbornePieceCol = -1;
    private int airbornePieceTimeRemaining = 0;
    
    // Game state
        private WinCondition winCondition;
    private boolean gameOver = false;
    
    private Board(Piece[][] grid, GameConfig config) {
        this.grid = grid;
        this.height = grid.length;
        this.width = grid.length > 0 ? grid[0].length : 0;
        this.config = config;
            this.winCondition = config.getWinCondition();
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
    
    // ========== Movement Handling ==========
    
    public void handleClick(int x, int y) {
        if (gameOver || movePending) {
            return;
        }
        
        CoordinateConverter converter = new CoordinateConverter(config.getPixelsPerCell());
        int col = converter.pixelToGridCol(x);
        int row = converter.pixelToGridRow(y);
        
        if (!isValid(row, col)) {
            return;
        }
        
        Piece target = getPieceAt(row, col);
        
        // No piece selected yet
        if (selectedRow == -1 || selectedCol == -1) {
            if (target != null) {
                selectedRow = row;
                selectedCol = col;
            }
            return;
        }
        
        Piece selectedPiece = getPieceAt(selectedRow, selectedCol);
        
        // Clicking same piece
        if (selectedRow == row && selectedCol == col) {
            return;
        }
        
        // Target is empty
        if (target == null) {
            if (isValidMove(selectedPiece, selectedRow, selectedCol, row, col)) {
                startMove(selectedPiece, selectedRow, selectedCol, row, col);
            }
            return;
        }
        
        // Target is same color -> deselect and select new piece
        if (selectedPiece.isSameColor(target)) {
            selectedRow = row;
            selectedCol = col;
            return;
        }
        
        // Try to capture
        if (isValidMove(selectedPiece, selectedRow, selectedCol, row, col)) {
            startMove(selectedPiece, selectedRow, selectedCol, row, col);
        } else {
            selectedRow = row;
            selectedCol = col;
        }
    }
    
    public void handleJump(int x, int y) {
        if (gameOver || airbornePiece != null || movePending) {
            return;
        }
        
        CoordinateConverter converter = new CoordinateConverter(config.getPixelsPerCell());
        int col = converter.pixelToGridCol(x);
        int row = converter.pixelToGridRow(y);
        
        if (!isValid(row, col)) {
            return;
        }
        
        Piece piece = getPieceAt(row, col);
        if (piece != null) {
            startJump(piece, row, col);
        }
    }
    
    public void handleWait(int ms) {
        if (gameOver) {
            return;
        }
        
        // Decrement timers
        if (movePending) {
            pendingTimeRemaining -= ms;
        }
        if (airbornePiece != null) {
            airbornePieceTimeRemaining -= ms;
        }
        
        // Handle move completion
        if (movePending && pendingTimeRemaining <= 0) {
            completePendingMove();
        }
        
        // Handle airborne landing
        if (airbornePiece != null && airbornePieceTimeRemaining <= 0) {
            completeJump();
        }
    }
    
    private void startMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol) {
        movePending = true;
        pendingPiece = piece;
        pendingFromRow = fromRow;
        pendingFromCol = fromCol;
        pendingToRow = toRow;
        pendingToCol = toCol;
        pendingTimeRemaining = config.getMoveDurationMs();
        selectedRow = -1;
        selectedCol = -1;
    }
    
    private void startJump(Piece piece, int row, int col) {
        airbornePiece = piece;
        airbornePieceRow = row;
        airbornePieceCol = col;
        airbornePieceTimeRemaining = config.getMoveDurationMs();
        grid[row][col] = null;
        selectedRow = -1;
        selectedCol = -1;
    }
    
    private void completePendingMove() {
        // Check for air capture
        if (airbornePiece != null && airbornePieceRow == pendingToRow &&
            airbornePieceCol == pendingToCol && airbornePiece.isOppositeColor(pendingPiece)) {
            // Air capture: moving piece is captured
            grid[pendingFromRow][pendingFromCol] = null;
            movePending = false;
            return;
        }
        
        // Normal move completion
        Piece destination = getPieceAt(pendingToRow, pendingToCol);
        Piece finalPiece = config.getPromotionRule().checkPromotion(pendingPiece, pendingToRow, this);
        
        // Check for king capture (win condition)
        if (destination != null && destination.getType().equals(PieceType.KING) &&
            destination.isOppositeColor(pendingPiece)) {
            grid[pendingFromRow][pendingFromCol] = null;
            grid[pendingToRow][pendingToCol] = finalPiece;
            winCondition.recordKingCapture(pendingPiece.getColor());
            gameOver = true;
            movePending = false;
            return;
        }
        
        // Standard move
        grid[pendingFromRow][pendingFromCol] = null;
        grid[pendingToRow][pendingToCol] = finalPiece;
        movePending = false;
    }
    
    private void completeJump() {
        grid[airbornePieceRow][airbornePieceCol] = airbornePiece;
        airbornePiece = null;
        airbornePieceRow = -1;
        airbornePieceCol = -1;
    }
    
    private boolean isValidMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow == toRow && fromCol == toCol) {
            return false;
        }
        
        // Can't move to airborne own piece
        if (airbornePiece != null && airbornePieceRow == toRow && airbornePieceCol == toCol &&
            airbornePiece.isSameColor(piece)) {
            return false;
        }
        
        // Get movement rule for this piece type
        MovementRule rule = config.getMovementRule(piece.getType());
        if (rule == null) {
            return false;
        }
        
        return rule.isValidMove(piece, fromRow, fromCol, toRow, toCol, this);
    }
    
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
        if (airbornePiece != null && airbornePieceRow == row && airbornePieceCol == col) {
            return airbornePiece;
        }
        return null;
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
}
