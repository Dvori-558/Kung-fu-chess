package rules;

import models.*;
import board.BoardContext;

/**
 * Unit tests for Movement Rules.
 * Tests each piece type's movement logic independently.
 */
public class MovementRulesTest {
    
    // Mock BoardContext for testing
    static class MockBoard implements BoardContext {
        private Piece[][] grid;
        
        MockBoard(int height, int width) {
            this.grid = new Piece[height][width];
        }
        
        void setPiece(int row, int col, Piece piece) {
            grid[row][col] = piece;
        }
        
        @Override
        public Piece getPieceAt(int row, int col) {
            if (!isValid(row, col)) return null;
            return grid[row][col];
        }
        
        @Override
        public boolean isValid(int row, int col) {
            return row >= 0 && row < grid.length && col >= 0 && col < grid[0].length;
        }
        
        @Override
        public boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
            int rowStep = Integer.compare(toRow, fromRow);
            int colStep = Integer.compare(toCol, fromCol);
            int row = fromRow + rowStep;
            int col = fromCol + colStep;
            
            while (row != toRow || col != toCol) {
                if (getPieceAt(row, col) != null) return false;
                row += rowStep;
                col += colStep;
            }
            return true;
        }
        
        @Override
        public int getHeight() {
            return grid.length;
        }
        
        @Override
        public int getWidth() {
            return grid.length > 0 ? grid[0].length : 0;
        }
        
        @Override
        public Piece getAirbornePieceAt(int row, int col) {
            return null;
        }
    }
    
    public static void main(String[] args) {
        int passed = 0;
        int total = 0;
        
        // Test Pawn: Single step forward
        total++;
        try {
            MockBoard board = new MockBoard(8, 8);
            Piece whitePawn = Piece.WHITE_PAWN;
            MovementRule pawnRule = new PawnMovementRule();
            
            assert pawnRule.isValidMove(whitePawn, 6, 0, 5, 0, board);
            assert !pawnRule.isValidMove(whitePawn, 6, 0, 7, 0, board);
            System.out.println("[PASS] Test 1: Pawn single step");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
        }
        
        // Test Pawn: Two steps from starting row
        total++;
        try {
            MockBoard board = new MockBoard(8, 8);
            Piece whitePawn = Piece.WHITE_PAWN;
            MovementRule pawnRule = new PawnMovementRule();
            
            assert pawnRule.isValidMove(whitePawn, 7, 0, 5, 0, board);
            assert !pawnRule.isValidMove(whitePawn, 6, 0, 4, 0, board);
            System.out.println("[PASS] Test 2: Pawn two steps");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 2: " + e.getMessage());
        }
        
        // Test Pawn: Diagonal capture
        total++;
        try {
            MockBoard board = new MockBoard(8, 8);
            Piece whitePawn = Piece.WHITE_PAWN;
            Piece blackPawn = Piece.BLACK_PAWN;
            board.setPiece(5, 1, blackPawn);
            
            MovementRule pawnRule = new PawnMovementRule();
            assert pawnRule.isValidMove(whitePawn, 6, 0, 5, 1, board);
            System.out.println("[PASS] Test 3: Pawn diagonal capture");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 3: " + e.getMessage());
        }
        
        // Test Knight: L-shape move
        total++;
        try {
            MockBoard board = new MockBoard(8, 8);
            Piece whiteKnight = Piece.WHITE_KNIGHT;
            MovementRule knightRule = new KnightMovementRule();
            
            assert knightRule.isValidMove(whiteKnight, 4, 4, 2, 5, board);
            assert knightRule.isValidMove(whiteKnight, 4, 4, 3, 6, board);
            assert !knightRule.isValidMove(whiteKnight, 4, 4, 4, 6, board);
            System.out.println("[PASS] Test 4: Knight L-shape");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 4: " + e.getMessage());
        }
        
        // Test King: One square in any direction
        total++;
        try {
            MockBoard board = new MockBoard(8, 8);
            Piece whiteKing = Piece.WHITE_KING;
            MovementRule kingRule = new KingMovementRule();
            
            assert kingRule.isValidMove(whiteKing, 4, 4, 3, 4, board);
            assert kingRule.isValidMove(whiteKing, 4, 4, 4, 5, board);
            assert kingRule.isValidMove(whiteKing, 4, 4, 5, 5, board);
            assert !kingRule.isValidMove(whiteKing, 4, 4, 2, 4, board);
            System.out.println("[PASS] Test 5: King movement");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 5: " + e.getMessage());
        }
        
        // Test Rook: Horizontal and vertical only
        total++;
        try {
            MockBoard board = new MockBoard(8, 8);
            Piece whiteRook = Piece.WHITE_ROOK;
            MovementRule rookRule = new RookMovementRule();
            
            assert rookRule.isValidMove(whiteRook, 4, 0, 4, 5, board);
            assert rookRule.isValidMove(whiteRook, 0, 4, 5, 4, board);
            assert !rookRule.isValidMove(whiteRook, 4, 4, 2, 2, board);
            System.out.println("[PASS] Test 6: Rook movement");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 6: " + e.getMessage());
        }
        
        // Test Bishop: Diagonal only
        total++;
        try {
            MockBoard board = new MockBoard(8, 8);
            Piece whiteBishop = Piece.WHITE_BISHOP;
            MovementRule bishopRule = new BishopMovementRule();
            
            assert bishopRule.isValidMove(whiteBishop, 4, 4, 2, 2, board);
            assert bishopRule.isValidMove(whiteBishop, 4, 4, 6, 6, board);
            assert !bishopRule.isValidMove(whiteBishop, 4, 4, 4, 6, board);
            System.out.println("[PASS] Test 7: Bishop movement");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 7: " + e.getMessage());
        }
        
        // Test Queen: Rook + Bishop
        total++;
        try {
            MockBoard board = new MockBoard(8, 8);
            Piece whiteQueen = Piece.WHITE_QUEEN;
            MovementRule queenRule = new QueenMovementRule();
            
            assert queenRule.isValidMove(whiteQueen, 4, 4, 4, 6, board);
            assert queenRule.isValidMove(whiteQueen, 4, 4, 2, 2, board);
            assert !queenRule.isValidMove(whiteQueen, 4, 4, 2, 5, board);
            System.out.println("[PASS] Test 8: Queen movement");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 8: " + e.getMessage());
        }
        
        // Test: Cannot capture own piece
        total++;
        try {
            MockBoard board = new MockBoard(8, 8);
            Piece whitePawn = Piece.WHITE_PAWN;
            Piece whiteBishop = Piece.WHITE_BISHOP;
            board.setPiece(5, 2, whiteBishop);
            
            MovementRule pawnRule = new PawnMovementRule();
            assert !pawnRule.isValidMove(whitePawn, 6, 1, 5, 2, board);
            System.out.println("[PASS] Test 9: Cannot capture own piece");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 9: " + e.getMessage());
        }
        
        // Test: Path must be clear
        total++;
        try {
            MockBoard board = new MockBoard(8, 8);
            Piece whiteRook = Piece.WHITE_ROOK;
            Piece blackPawn = Piece.BLACK_PAWN;
            board.setPiece(4, 2, blackPawn);
            
            MovementRule rookRule = new RookMovementRule();
            assert !rookRule.isValidMove(whiteRook, 4, 0, 4, 5, board);
            assert rookRule.isValidMove(whiteRook, 4, 0, 4, 2, board);
            System.out.println("[PASS] Test 10: Path clear check");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 10: " + e.getMessage());
        }
        
        System.out.println("\n=== Movement Rules Tests: " + passed + "/" + total + " passed ===");
    }
}
