package tests;

import models.Piece;
import models.PieceType;
import board.Board;
import rules.GameConfig;
import rules.RuleEngine;
import rules.StandardRuleEngine;
import rules.MoveValidation;

/**
 * Unit tests for RuleEngine validation.
 * Tests all stable reason strings and read-only behavior.
 */
public class RuleEngineTest {
    
    public static void main(String[] args) {
        int passed = 0;
        int total = 0;
        
        GameConfig config = new GameConfig.Builder().buildStandardChess().build();
        RuleEngine engine = new StandardRuleEngine(config);
        
        // Test 1: Valid rook move (horizontal)
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            grid[0][0] = rook;
            Board board = Board.create(grid, config);
            
            MoveValidation result = engine.validateMove(board, 0, 0, 0, 2);
            assert result.isValid() : "Valid rook move should return true";
            assert result.getReason().equals("ok") : "Reason should be 'ok'";
            System.out.println("[PASS] Test 1: Valid rook move");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
        }
        
        // Test 2: Outside board - source
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            grid[0][0] = rook;
            Board board = Board.create(grid, config);
            
            MoveValidation result = engine.validateMove(board, -1, 0, 0, 2);
            assert !result.isValid() : "Move outside board should be invalid";
            assert result.getReason().equals(MoveValidation.OUTSIDE_BOARD) : 
                "Reason should be 'outside_board', got: " + result.getReason();
            System.out.println("[PASS] Test 2: Outside board - source");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 2: " + e.getMessage());
        }
        
        // Test 3: Outside board - destination
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            grid[0][0] = rook;
            Board board = Board.create(grid, config);
            
            MoveValidation result = engine.validateMove(board, 0, 0, 0, 5);
            assert !result.isValid() : "Move outside board should be invalid";
            assert result.getReason().equals(MoveValidation.OUTSIDE_BOARD) : 
                "Reason should be 'outside_board'";
            System.out.println("[PASS] Test 3: Outside board - destination");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 3: " + e.getMessage());
        }
        
        // Test 4: Empty source
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Board board = Board.create(grid, config);
            
            MoveValidation result = engine.validateMove(board, 0, 0, 0, 2);
            assert !result.isValid() : "Move from empty cell should be invalid";
            assert result.getReason().equals(MoveValidation.EMPTY_SOURCE) : 
                "Reason should be 'empty_source'";
            System.out.println("[PASS] Test 4: Empty source");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 4: " + e.getMessage());
        }
        
        // Test 5: Friendly destination
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            Piece pawn = new Piece(Piece.WHITE, PieceType.PAWN);
            grid[0][0] = rook;
            grid[0][2] = pawn;
            Board board = Board.create(grid, config);
            
            MoveValidation result = engine.validateMove(board, 0, 0, 0, 2);
            assert !result.isValid() : "Move to friendly piece should be invalid";
            assert result.getReason().equals(MoveValidation.FRIENDLY_DESTINATION) : 
                "Reason should be 'friendly_destination'";
            System.out.println("[PASS] Test 5: Friendly destination");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 5: " + e.getMessage());
        }
        
        // Test 6: Illegal piece move (rook diagonal)
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            grid[0][0] = rook;
            Board board = Board.create(grid, config);
            
            MoveValidation result = engine.validateMove(board, 0, 0, 2, 2);
            assert !result.isValid() : "Rook diagonal move should be invalid";
            assert result.getReason().equals(MoveValidation.ILLEGAL_PIECE_MOVE) : 
                "Reason should be 'illegal_piece_move'";
            System.out.println("[PASS] Test 6: Illegal piece move");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 6: " + e.getMessage());
        }
        
        // Test 7: Capture enemy (rook captures enemy pawn)
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            Piece enemy = new Piece(Piece.BLACK, PieceType.PAWN);
            grid[0][0] = rook;
            grid[0][2] = enemy;
            Board board = Board.create(grid, config);
            
            MoveValidation result = engine.validateMove(board, 0, 0, 0, 2);
            assert result.isValid() : "Capture of enemy should be valid";
            assert result.getReason().equals("ok") : "Reason should be 'ok'";
            System.out.println("[PASS] Test 7: Capture enemy");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 7: " + e.getMessage());
        }
        
        // Test 8: Path blocked
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            Piece blocker = new Piece(Piece.WHITE, PieceType.PAWN);
            grid[0][0] = rook;
            grid[0][1] = blocker;
            grid[0][2] = null;
            Board board = Board.create(grid, config);
            
            MoveValidation result = engine.validateMove(board, 0, 0, 0, 2);
            assert !result.isValid() : "Move through blocker should be invalid";
            assert result.getReason().equals(MoveValidation.ILLEGAL_PIECE_MOVE) : 
                "Reason should be 'illegal_piece_move' (path not clear)";
            System.out.println("[PASS] Test 8: Path blocked");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 8: " + e.getMessage());
        }
        
        // Test 9: Bishop valid diagonal
        total++;
        try {
            Piece[][] grid = new Piece[4][4];
            Piece bishop = new Piece(Piece.WHITE, PieceType.BISHOP);
            grid[0][0] = bishop;
            Board board = Board.create(grid, config);
            
            MoveValidation result = engine.validateMove(board, 0, 0, 3, 3);
            assert result.isValid() : "Bishop diagonal should be valid";
            assert result.getReason().equals("ok") : "Reason should be 'ok'";
            System.out.println("[PASS] Test 9: Bishop diagonal");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 9: " + e.getMessage());
        }
        
        // Test 10: RuleEngine does not mutate board
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            Piece enemy = new Piece(Piece.BLACK, PieceType.KING);
            grid[0][0] = rook;
            grid[0][2] = enemy;
            Board board = Board.create(grid, config);
            
            MoveValidation result = engine.validateMove(board, 0, 0, 0, 2);
            assert result.isValid() : "Move should be valid";
            
            // Board must not have changed
            assert board.getPieceAt(0, 0) == rook : "Source piece should still be there";
            assert board.getPieceAt(0, 2) == enemy : "Target piece should still be there";
            System.out.println("[PASS] Test 10: RuleEngine does not mutate board");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 10: " + e.getMessage());
        }
        
        System.out.println("\n=== RuleEngine Tests: " + passed + "/" + total + " passed ===");
    }
}
