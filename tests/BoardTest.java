package tests;

import models.Piece;
import models.PieceType;
import board.Board;
import rules.GameConfig;

/**
 * Unit tests for Board model layer.
 * Tests board dimensions, piece lookup, occupancy, and state management.
 * Board is a pure model — no pixels, no rendering, no game logic.
 */
public class BoardTest {
    
    public static void main(String[] args) {
        int passed = 0;
        int total = 0;
        
        GameConfig config = new GameConfig.Builder().buildStandardChess().build();
        
        // Test 1: Board width and height
        total++;
        try {
            Piece[][] grid = new Piece[3][4];
            Board board = Board.create(grid, config);
            
            assert board.getHeight() == 3 : "Height should be 3";
            assert board.getWidth() == 4 : "Width should be 4";
            System.out.println("[PASS] Test 1: Board dimensions");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
        }
        
        // Test 2: Empty cell returns null
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Board board = Board.create(grid, config);
            
            Piece piece = board.getPieceAt(0, 0);
            assert piece == null : "Empty cell should return null";
            System.out.println("[PASS] Test 2: Empty cell lookup");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 2: " + e.getMessage());
        }
        
        // Test 3: Piece lookup
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece king = new Piece(Piece.WHITE, PieceType.KING);
            grid[1][1] = king;
            Board board = Board.create(grid, config);
            
            Piece found = board.getPieceAt(1, 1);
            assert found == king : "Piece lookup should return the same piece";
            assert found.getColor() == Piece.WHITE : "Piece color should be white";
            assert found.getType().equals(PieceType.KING) : "Piece type should be KING";
            System.out.println("[PASS] Test 3: Piece lookup");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 3: " + e.getMessage());
        }
        
        // Test 4: Out of bounds returns null
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Board board = Board.create(grid, config);
            
            Piece piece1 = board.getPieceAt(-1, 0);
            Piece piece2 = board.getPieceAt(0, -1);
            Piece piece3 = board.getPieceAt(5, 5);
            
            assert piece1 == null : "Out of bounds should return null";
            assert piece2 == null : "Out of bounds should return null";
            assert piece3 == null : "Out of bounds should return null";
            System.out.println("[PASS] Test 4: Out of bounds");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 4: " + e.getMessage());
        }
        
        // Test 5: isValid bounds check
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Board board = Board.create(grid, config);
            
            assert board.isValid(0, 0) : "Valid cell";
            assert board.isValid(2, 2) : "Valid cell";
            assert !board.isValid(-1, 0) : "Invalid cell (row)";
            assert !board.isValid(0, -1) : "Invalid cell (col)";
            assert !board.isValid(3, 0) : "Invalid cell (row out of bounds)";
            assert !board.isValid(0, 3) : "Invalid cell (col out of bounds)";
            System.out.println("[PASS] Test 5: isValid bounds check");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 5: " + e.getMessage());
        }
        
        // Test 6: Multiple pieces on board
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece king = new Piece(Piece.WHITE, PieceType.KING);
            Piece rook = new Piece(Piece.BLACK, PieceType.ROOK);
            Piece pawn = new Piece(Piece.WHITE, PieceType.PAWN);
            
            grid[0][0] = king;
            grid[1][1] = rook;
            grid[2][2] = pawn;
            
            Board board = Board.create(grid, config);
            
            assert board.getPieceAt(0, 0) == king;
            assert board.getPieceAt(1, 1) == rook;
            assert board.getPieceAt(2, 2) == pawn;
            assert board.getPieceAt(0, 1) == null;
            System.out.println("[PASS] Test 6: Multiple pieces");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 6: " + e.getMessage());
        }
        
        // Test 7: Empty board
        total++;
        try {
            Piece[][] grid = new Piece[0][0];
            Board board = Board.create(grid, config);
            
            assert board.isEmpty() : "Empty grid should be empty";
            assert board.getHeight() == 0 : "Empty board height";
            assert board.getWidth() == 0 : "Empty board width";
            System.out.println("[PASS] Test 7: Empty board");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 7: " + e.getMessage());
        }
        
        // Test 8: isPathClear horizontal
        total++;
        try {
            Piece[][] grid = new Piece[3][4];
            Board board = Board.create(grid, config);
            
            assert board.isPathClear(0, 0, 0, 3) : "Clear horizontal path";
            
            Piece blocker = new Piece(Piece.WHITE, PieceType.PAWN);
            grid[0][1] = blocker;
            board = Board.create(grid, config);
            
            assert !board.isPathClear(0, 0, 0, 3) : "Blocked horizontal path";
            assert board.isPathClear(0, 0, 0, 1) : "Path to blocker is clear";
            System.out.println("[PASS] Test 8: isPathClear horizontal");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 8: " + e.getMessage());
        }
        
        // Test 9: isPathClear vertical
        total++;
        try {
            Piece[][] grid = new Piece[4][3];
            Board board = Board.create(grid, config);
            
            assert board.isPathClear(0, 0, 3, 0) : "Clear vertical path";
            
            Piece blocker = new Piece(Piece.WHITE, PieceType.PAWN);
            grid[2][0] = blocker;
            board = Board.create(grid, config);
            
            assert !board.isPathClear(0, 0, 3, 0) : "Blocked vertical path";
            System.out.println("[PASS] Test 9: isPathClear vertical");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 9: " + e.getMessage());
        }
        
        // Test 10: isPathClear diagonal
        total++;
        try {
            Piece[][] grid = new Piece[4][4];
            Board board = Board.create(grid, config);
            
            assert board.isPathClear(0, 0, 3, 3) : "Clear diagonal path";
            
            Piece blocker = new Piece(Piece.WHITE, PieceType.PAWN);
            grid[1][1] = blocker;
            board = Board.create(grid, config);
            
            assert !board.isPathClear(0, 0, 3, 3) : "Blocked diagonal path";
            System.out.println("[PASS] Test 10: isPathClear diagonal");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 10: " + e.getMessage());
        }
        
        System.out.println("\n=== Board Tests: " + passed + "/" + total + " passed ===");
    }
}
