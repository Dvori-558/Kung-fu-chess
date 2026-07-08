package tests;

import board.Board;
import board.MoveManager;
import board.AirborneManager;
import board.PromotionService;
import board.WinManager;
import models.Piece;
import models.PieceType;
import rules.GameConfig;

public class MoveManagerTest {
    public static void main(String[] args) {
        int passed = 0, total = 0;

        // Test 1: Basic move completion
        total++;
        try {
            GameConfig config = new GameConfig.Builder().buildStandardChess().build();
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            grid[0][0] = rook;
            Board b = Board.create(grid, config);

            PromotionService ps = new PromotionService(config.getPromotionRule());
            WinManager wm = new WinManager(config.getWinCondition());
            AirborneManager am = new AirborneManager(b, config);
            MoveManager mm = new MoveManager(b, config, ps, wm, am);

            mm.startMove(rook, 0, 0, 0, 2);
            // before tick, source should still hold
            assert b.getPieceAt(0,0) != null;
            mm.tick(config.getMoveDurationMs());
            assert b.getPieceAt(0,2) != null;

            System.out.println("[PASS] Test 1: Basic move completion");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
        }

        // Test 2: King capture -> win
        total++;
        try {
            GameConfig config = new GameConfig.Builder().buildStandardChess().build();
            Piece[][] grid = new Piece[3][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            Piece blackKing = new Piece(Piece.BLACK, PieceType.KING);
            grid[0][0] = rook;
            grid[0][2] = blackKing;
            Board b = Board.create(grid, config);

            PromotionService ps = new PromotionService(config.getPromotionRule());
            WinManager wm = new WinManager(config.getWinCondition());
            AirborneManager am = new AirborneManager(b, config);
            MoveManager mm = new MoveManager(b, config, ps, wm, am);

            mm.startMove(rook, 0, 0, 0, 2);
            mm.tick(config.getMoveDurationMs());

            assert wm.isGameOver();
            assert wm.getWinner() != null && wm.getWinner() == Piece.WHITE;

            System.out.println("[PASS] Test 2: King capture records win");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 2: " + e.getMessage());
        }

        System.out.println("\n=== MoveManager Tests: " + passed + "/" + total + " passed ===");
    }
}
