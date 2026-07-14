package tests;

import models.*;
import board.Board;
import rules.*;

/**
 * Unit tests for piece movement rules via RuleEngine.
 * Per design guide: PieceRules (Strategy pattern) are tested through RuleEngine.
 */
public class MovementRulesTest {

    public static void main(String[] args) {
        int passed = 0;
        int total = 0;

        GameConfig config = new GameConfig.Builder().buildStandardChess().build();
        RuleEngine engine = new StandardRuleEngine(config);

        // Test 1: Pawn single step forward
        total++;
        try {
            Piece[][] grid = new Piece[8][8];
            grid[6][0] = new Piece(Piece.WHITE, PieceType.PAWN);
            Board board = Board.create(grid, config);
            assert engine.validateMove(board, 6, 0, 5, 0).isValid() : "Pawn forward";
            assert !engine.validateMove(board, 6, 0, 7, 0).isValid() : "Pawn backward";
            System.out.println("[PASS] Test 1: Pawn single step");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
        }

        // Test 2: Pawn two steps from starting row
        total++;
        try {
            Piece[][] grid = new Piece[8][8];
            grid[7][0] = new Piece(Piece.WHITE, PieceType.PAWN);
            Board board = Board.create(grid, config);
            assert engine.validateMove(board, 7, 0, 5, 0).isValid() : "Pawn two steps from row 7";
            System.out.println("[PASS] Test 2: Pawn two steps");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 2: " + e.getMessage());
        }

        // Test 3: Pawn diagonal capture
        total++;
        try {
            Piece[][] grid = new Piece[8][8];
            grid[6][0] = new Piece(Piece.WHITE, PieceType.PAWN);
            grid[5][1] = new Piece(Piece.BLACK, PieceType.PAWN);
            Board board = Board.create(grid, config);
            assert engine.validateMove(board, 6, 0, 5, 1).isValid() : "Pawn diagonal capture";
            System.out.println("[PASS] Test 3: Pawn diagonal capture");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 3: " + e.getMessage());
        }

        // Test 4: Knight L-shape
        total++;
        try {
            Piece[][] grid = new Piece[8][8];
            grid[4][4] = new Piece(Piece.WHITE, PieceType.KNIGHT);
            Board board = Board.create(grid, config);
            assert engine.validateMove(board, 4, 4, 2, 5).isValid() : "Knight L-shape 1";
            assert engine.validateMove(board, 4, 4, 3, 6).isValid() : "Knight L-shape 2";
            assert !engine.validateMove(board, 4, 4, 4, 6).isValid() : "Knight invalid";
            System.out.println("[PASS] Test 4: Knight L-shape");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 4: " + e.getMessage());
        }

        // Test 5: King one square
        total++;
        try {
            Piece[][] grid = new Piece[8][8];
            grid[4][4] = new Piece(Piece.WHITE, PieceType.KING);
            Board board = Board.create(grid, config);
            assert engine.validateMove(board, 4, 4, 3, 4).isValid() : "King up";
            assert engine.validateMove(board, 4, 4, 5, 5).isValid() : "King diagonal";
            assert !engine.validateMove(board, 4, 4, 2, 4).isValid() : "King two squares";
            System.out.println("[PASS] Test 5: King movement");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 5: " + e.getMessage());
        }

        // Test 6: Rook horizontal and vertical only
        total++;
        try {
            Piece[][] grid = new Piece[8][8];
            grid[4][0] = new Piece(Piece.WHITE, PieceType.ROOK);
            Board board = Board.create(grid, config);
            assert engine.validateMove(board, 4, 0, 4, 5).isValid() : "Rook horizontal";
            assert engine.validateMove(board, 4, 0, 7, 0).isValid() : "Rook vertical";
            assert !engine.validateMove(board, 4, 0, 2, 2).isValid() : "Rook diagonal invalid";
            System.out.println("[PASS] Test 6: Rook movement");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 6: " + e.getMessage());
        }

        // Test 7: Bishop diagonal only
        total++;
        try {
            Piece[][] grid = new Piece[8][8];
            grid[4][4] = new Piece(Piece.WHITE, PieceType.BISHOP);
            Board board = Board.create(grid, config);
            assert engine.validateMove(board, 4, 4, 2, 2).isValid() : "Bishop diagonal";
            assert engine.validateMove(board, 4, 4, 6, 6).isValid() : "Bishop diagonal 2";
            assert !engine.validateMove(board, 4, 4, 4, 6).isValid() : "Bishop straight invalid";
            System.out.println("[PASS] Test 7: Bishop movement");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 7: " + e.getMessage());
        }

        // Test 8: Queen rook + bishop
        total++;
        try {
            Piece[][] grid = new Piece[8][8];
            grid[4][4] = new Piece(Piece.WHITE, PieceType.QUEEN);
            Board board = Board.create(grid, config);
            assert engine.validateMove(board, 4, 4, 4, 6).isValid() : "Queen horizontal";
            assert engine.validateMove(board, 4, 4, 2, 2).isValid() : "Queen diagonal";
            assert !engine.validateMove(board, 4, 4, 2, 5).isValid() : "Queen L-shape invalid";
            System.out.println("[PASS] Test 8: Queen movement");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 8: " + e.getMessage());
        }

        // Test 9: Cannot capture own piece
        total++;
        try {
            Piece[][] grid = new Piece[8][8];
            grid[4][4] = new Piece(Piece.WHITE, PieceType.ROOK);
            grid[4][6] = new Piece(Piece.WHITE, PieceType.PAWN);
            Board board = Board.create(grid, config);
            MoveValidation result = engine.validateMove(board, 4, 4, 4, 6);
            assert !result.isValid() : "Cannot capture own piece";
            assert result.getReason().equals(MoveValidation.FRIENDLY_DESTINATION);
            System.out.println("[PASS] Test 9: Cannot capture own piece");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 9: " + e.getMessage());
        }

        // Test 10: Path must be clear for sliding pieces
        total++;
        try {
            Piece[][] grid = new Piece[8][8];
            grid[4][0] = new Piece(Piece.WHITE, PieceType.ROOK);
            grid[4][2] = new Piece(Piece.BLACK, PieceType.PAWN);
            Board board = Board.create(grid, config);
            assert !engine.validateMove(board, 4, 0, 4, 5).isValid() : "Blocked path invalid";
            assert engine.validateMove(board, 4, 0, 4, 2).isValid() : "Capture blocker valid";
            System.out.println("[PASS] Test 10: Path clear check");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 10: " + e.getMessage());
        }

        System.out.println("\n=== Movement Rules Tests: " + passed + "/" + total + " passed ===");
    }
}
