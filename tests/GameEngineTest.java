package tests;

import board.Board;
import engine.ClickCommand;
import engine.WaitCommand;
import rules.GameConfig;
import models.Piece;
import models.PieceType;

public class GameEngineTest {
    public static void main(String[] args) {
        int passed = 0, total = 0;

        total++;
        try {
            GameConfig config = new GameConfig.Builder().buildStandardChess().build();
            Piece[][] grid = new Piece[8][8];
            Piece pawn = new Piece(Piece.WHITE, PieceType.PAWN);
            grid[6][0] = pawn; // white pawn at row 6
            Board b = Board.create(grid, config);

            engine.GameEngineImpl engine = new engine.GameEngineImpl(b, config);

            // Click on pawn
            engine.handle(new ClickCommand(0 * config.getPixelsPerCell() + 1, 6 * config.getPixelsPerCell() + 1));
            // Click forward
            engine.handle(new ClickCommand(0 * config.getPixelsPerCell() + 1, 5 * config.getPixelsPerCell() + 1));
            // Wait to complete
            engine.handle(new WaitCommand(config.getMoveDurationMs()));

            if (b.getPieceAt(5,0) == null) throw new RuntimeException("Pawn did not move to expected square");

            System.out.println("[PASS] Test 1: Engine click->move flow");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
        }

        System.out.println("\n=== GameEngine Tests: " + passed + "/" + total + " passed ===");
    }
}
