package tests;

import board.Board;
import board.AirborneManager;
import models.Piece;
import models.PieceType;
import rules.GameConfig;

public class AirborneManagerTest {
    public static void main(String[] args) {
        int passed = 0, total = 0;

        total++;
        try {
            GameConfig config = new GameConfig.Builder().buildStandardChess().build();
            Piece[][] grid = new Piece[3][3];
            Piece pawn = new Piece(Piece.WHITE, PieceType.PAWN);
            grid[1][1] = pawn;
            Board b = Board.create(grid, config);

            AirborneManager am = new AirborneManager(b, config);
            am.startJump(pawn, 1, 1);
            if (b.getPieceAt(1,1) != null) throw new RuntimeException("Piece not removed on jump");
            am.tick(config.getMoveDurationMs());
            if (b.getPieceAt(1,1) == null) throw new RuntimeException("Piece did not land back after tick");

            System.out.println("[PASS] Test 1: Jump remove and land");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
        }

        System.out.println("\n=== Airborne Manager Tests: " + passed + "/" + total + " passed ===");
    }
}
