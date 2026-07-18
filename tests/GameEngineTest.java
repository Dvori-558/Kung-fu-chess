package tests;

import models.Piece;
import models.PieceType;
import board.Board;
import engine.GameEngine;
import engine.GameEngineImpl;
import engine.MoveResult;
import rules.GameConfig;

/**
 * Unit tests for GameEngine coordinator layer.
 * Tests application-level guards and delegation behavior only.
 * Does NOT test motion details (that's RealTimeArbiterTest).
 * Does NOT test rule validation (that's RuleEngineTest).
 */
public class GameEngineTest {
    
    public static void main(String[] args) {
        int passed = 0;
        int total = 0;
        
        GameConfig config = new GameConfig.Builder().buildStandardChess().build();
        
        // Test 1: Valid move returns ok
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            grid[0][0] = rook;
            Board board = Board.create(grid, config);
            GameEngine engine = new GameEngineImpl(board, config);
            
            MoveResult result = engine.requestMove(0, 0, 0, 2);
            assert result.isAccepted() : "Valid move should be accepted";
            assert result.getReason().equals(MoveResult.OK) : "Reason should be 'ok'";
            System.out.println("[PASS] Test 1: Valid move accepted");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
        }
        
        // Test 2: game_over guard rejects move
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            Piece blackKing = new Piece(Piece.BLACK, PieceType.KING);
            grid[0][0] = rook;
            grid[0][2] = blackKing;
            Board board = Board.create(grid, config);
            GameEngine engine = new GameEngineImpl(board, config);
            
            // Start capture move
            MoveResult result1 = engine.requestMove(0, 0, 0, 2);
            assert result1.isAccepted() : "First move should be accepted";
            
            // Advance time to complete motion and trigger king capture
            engine.pause(2000);
            
            // Now game is over. Try another move.
            MoveResult result2 = engine.requestMove(0, 0, 1, 0);
            assert !result2.isAccepted() : "Move after game over should be rejected";
            assert result2.getReason().equals(MoveResult.GAME_OVER) : 
                "Reason should be 'game_over', got: " + result2.getReason();
            System.out.println("[PASS] Test 2: game_over guard");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 2: " + e.getMessage());
        }

        // Reset config because win condition strategy is stateful across engines.
        config = new GameConfig.Builder().buildStandardChess().build();
        
        // Test 3: motion_in_progress guard rejects second move
        total++;
        try {
            Piece[][] grid = new Piece[4][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            Piece pawn = new Piece(Piece.WHITE, PieceType.PAWN);
            grid[0][0] = rook;
            grid[1][0] = pawn;
            Board board = Board.create(grid, config);
            GameEngine engine = new GameEngineImpl(board, config);
            
            // Start first move
            MoveResult result1 = engine.requestMove(0, 0, 0, 2);
            assert result1.isAccepted() : "First move should be accepted";
            
            // Try to start second move while first is in progress
            MoveResult result2 = engine.requestMove(1, 0, 2, 0);
            assert !result2.isAccepted() : "Move while motion in progress should be rejected";
            assert result2.getReason().equals(MoveResult.MOTION_IN_PROGRESS) : 
                "Reason should be 'motion_in_progress', got: " + result2.getReason();
            System.out.println("[PASS] Test 3: motion_in_progress guard");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 3: " + e.getMessage());
        }
        
        // Test 4: Invalid move (outside board) returns rule-level reason
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            grid[0][0] = rook;
            Board board = Board.create(grid, config);
            GameEngine engine = new GameEngineImpl(board, config);
            
            MoveResult result = engine.requestMove(-1, 0, 0, 2);
            assert !result.isAccepted() : "Move outside board should be rejected";
            assert result.getReason().equals("outside_board") : 
                "Reason should be 'outside_board', got: " + result.getReason();
            System.out.println("[PASS] Test 4: Invalid move - outside_board reason");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 4: " + e.getMessage());
        }
        
        // Test 5: Invalid move (empty source) returns rule-level reason
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Board board = Board.create(grid, config);
            GameEngine engine = new GameEngineImpl(board, config);
            
            MoveResult result = engine.requestMove(0, 0, 0, 2);
            assert !result.isAccepted() : "Move from empty cell should be rejected";
            assert result.getReason().equals("empty_source") : 
                "Reason should be 'empty_source', got: " + result.getReason();
            System.out.println("[PASS] Test 5: Invalid move - empty_source reason");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 5: " + e.getMessage());
        }
        
        // Test 6: Invalid move (friendly destination) returns rule-level reason
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            Piece pawn = new Piece(Piece.WHITE, PieceType.PAWN);
            grid[0][0] = rook;
            grid[0][2] = pawn;
            Board board = Board.create(grid, config);
            GameEngine engine = new GameEngineImpl(board, config);
            
            MoveResult result = engine.requestMove(0, 0, 0, 2);
            assert !result.isAccepted() : "Move to friendly destination should be rejected";
            assert result.getReason().equals("friendly_destination") : 
                "Reason should be 'friendly_destination', got: " + result.getReason();
            System.out.println("[PASS] Test 6: Invalid move - friendly_destination reason");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 6: " + e.getMessage());
        }
        
        // Test 7: Invalid move (illegal piece move) returns rule-level reason
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            grid[0][0] = rook;
            Board board = Board.create(grid, config);
            GameEngine engine = new GameEngineImpl(board, config);
            
            // Rook cannot move diagonally
            MoveResult result = engine.requestMove(0, 0, 2, 2);
            assert !result.isAccepted() : "Illegal rook move should be rejected";
            assert result.getReason().equals("illegal_piece_move") : 
                "Reason should be 'illegal_piece_move', got: " + result.getReason();
            System.out.println("[PASS] Test 7: Invalid move - illegal_piece_move reason");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 7: " + e.getMessage());
        }
        
        // Test 8: After motion completes, next move is accepted
        total++;
        try {
            Piece[][] grid = new Piece[4][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            Piece pawn = new Piece(Piece.WHITE, PieceType.PAWN);
            grid[0][0] = rook;
            grid[2][0] = pawn;
            Board board = Board.create(grid, config);
            GameEngine engine = new GameEngineImpl(board, config);
            
            // Start and complete first move
            MoveResult result1 = engine.requestMove(0, 0, 0, 1);
            assert result1.isAccepted();
            
            // Wait for move to complete + rest cooldown
            engine.pause(2400);
            
            // Second move should now be accepted (different piece)
            MoveResult result2 = engine.requestMove(2, 0, 1, 0);
            assert result2.isAccepted() : "Move after motion completes should be accepted";
            System.out.println("[PASS] Test 8: Move accepted after motion completes");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 8: " + e.getMessage());
        }
        
        // Test 9: Snapshot is read-only
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece king = new Piece(Piece.WHITE, PieceType.KING);
            grid[1][1] = king;
            Board board = Board.create(grid, config);
            GameEngine engine = new GameEngineImpl(board, config);
            
            var snapshot = engine.snapshot();
            assert !snapshot.isGameOver() : "Game should not be over";
            assert snapshot.getBoard() != null : "Snapshot should have board";
            System.out.println("[PASS] Test 9: Snapshot created");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 9: " + e.getMessage());
        }
        
        // Test 10: pause(ms) is safe after game over
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            Piece blackKing = new Piece(Piece.BLACK, PieceType.KING);
            grid[0][0] = rook;
            grid[0][1] = blackKing;
            Board board = Board.create(grid, config);
            GameEngine engine = new GameEngineImpl(board, config);
            
            // Capture and end game
            engine.requestMove(0, 0, 0, 1);
            engine.pause(2000);
            
            // pause() should not crash after game over
            engine.pause(1000);
            System.out.println("[PASS] Test 10: pause() safe after game over");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 10: " + e.getMessage());
        }

        // Reset config because win condition strategy is stateful across engines.
        config = new GameConfig.Builder().buildStandardChess().build();

        // Test 11: piece-specific speed affects move duration
        total++;
        try {
            GameConfig fastRookConfig = new GameConfig.Builder()
                    .buildStandardChess()
                    .pieceMoveSpeed(PieceType.ROOK, 2.0)
                    .build();

            Piece[][] grid = new Piece[1][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            grid[0][0] = rook;
            Board board = Board.create(grid, fastRookConfig);
            GameEngine engine = new GameEngineImpl(board, fastRookConfig);

            MoveResult result = engine.requestMove(0, 0, 0, 2);
            assert result.isAccepted() : "Configured-speed move should be accepted";

            engine.pause(900);
            assert board.getPieceAt(0, 0) != null : "Rook should still be in transit before 1000ms";
            assert board.getPieceAt(0, 2) == null : "Rook should not have arrived yet";

            engine.pause(200);
            assert board.getPieceAt(0, 0) == null : "Rook should leave source after configured duration";
            assert board.getPieceAt(0, 2) != null : "Rook should arrive at destination after configured duration";

            System.out.println("[PASS] Test 11: piece-specific speed controls duration");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 11: " + e.getMessage());
        }

        // Test 12: rest cooldown rejects immediate move after arrival
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            grid[0][0] = rook;
            Board board = Board.create(grid, config);
            GameEngine engine = new GameEngineImpl(board, config);

            MoveResult first = engine.requestMove(0, 0, 0, 1);
            assert first.isAccepted() : "First move should be accepted";
            engine.pause(1000);

            MoveResult immediate = engine.requestMove(0, 1, 0, 2);
            assert !immediate.isAccepted() : "Move during rest should be rejected";
            assert immediate.getReason().equals(MoveResult.REST_IN_PROGRESS)
                    : "Expected rest_in_progress, got: " + immediate.getReason();

            engine.pause(1300);
            MoveResult afterRest = engine.requestMove(0, 1, 0, 2);
            assert afterRest.isAccepted() : "Move after rest should be accepted";

            System.out.println("[PASS] Test 12: rest cooldown blocks immediate follow-up move");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 12: " + e.getMessage());
        }

        // Test 13: rest cooldown rejects immediate jump after arrival
        total++;
        try {
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            grid[0][0] = rook;
            Board board = Board.create(grid, config);
            GameEngine engine = new GameEngineImpl(board, config);

            MoveResult first = engine.requestMove(0, 0, 0, 1);
            assert first.isAccepted() : "First move should be accepted";
            engine.pause(1000);

            boolean jumpImmediate = engine.requestJump(0, 1);
            assert !jumpImmediate : "Jump during rest should be rejected";

            engine.pause(1300);
            boolean jumpAfterRest = engine.requestJump(0, 1);
            assert jumpAfterRest : "Jump after rest should be accepted";

            System.out.println("[PASS] Test 13: rest cooldown blocks immediate jump");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 13: " + e.getMessage());
        }
        
        System.out.println("\n=== GameEngine Tests (Coordinator): " + passed + "/" + total + " passed ===");
    }
}
