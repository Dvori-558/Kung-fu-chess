package tests;

import board.Board;
import board.Controller;
import engine.GameEngine;
import engine.GameEngineImpl;
import models.Piece;
import models.PieceType;
import rules.GameConfig;
import ui.SpriteAssetCatalog;
import ui.VisualSnapshot;
import ui.VisualSnapshotFactory;
import utils.CoordinateConverter;

/**
 * Smoke tests for UI visual snapshot pipeline.
 */
public class VisualSnapshotFactoryTest {
    public static void main(String[] args) {
        int passed = 0;
        int total = 0;

        // Test 1: click selection is reflected in VisualSnapshot
        total++;
        try {
            GameConfig config = new GameConfig.Builder().buildStandardChess().build();
            Piece[][] grid = new Piece[3][3];
            grid[0][0] = new Piece(Piece.WHITE, PieceType.ROOK);
            Board board = Board.create(grid, config);

            GameEngine engine = new GameEngineImpl(board, config);
            Controller controller = new Controller(engine, board, new CoordinateConverter(config.getPixelsPerCell()));

            SpriteAssetCatalog assets = new SpriteAssetCatalog(config.getPixelsPerCell());
            VisualSnapshotFactory factory = new VisualSnapshotFactory(
                    engine,
                    controller,
                    config.getPixelsPerCell(),
                    assets
            );

            controller.click(50, 50);
            VisualSnapshot snapshot = factory.build(0L);

            assert snapshot.getSelectedRow() == 0 : "Selected row should be 0";
            assert snapshot.getSelectedCol() == 0 : "Selected col should be 0";
            System.out.println("[PASS] Test 1: selection is reflected in snapshot");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
        }

        // Test 2: moving piece appears as interpolated visual snapshot
        total++;
        try {
            GameConfig config = new GameConfig.Builder().buildStandardChess().build();
            Piece[][] grid = new Piece[3][4];
            grid[0][0] = new Piece(Piece.WHITE, PieceType.ROOK);
            Board board = Board.create(grid, config);

            GameEngine engine = new GameEngineImpl(board, config);
            Controller controller = new Controller(engine, board, new CoordinateConverter(config.getPixelsPerCell()));

            SpriteAssetCatalog assets = new SpriteAssetCatalog(config.getPixelsPerCell());
            VisualSnapshotFactory factory = new VisualSnapshotFactory(
                    engine,
                    controller,
                    config.getPixelsPerCell(),
                    assets
            );

            controller.click(50, 50);
            controller.click(250, 50);
            engine.pause(500);

            VisualSnapshot snapshot = factory.build(500L);
            boolean foundMoving = false;
            for (var p : snapshot.getPieces()) {
                if (p.getPiece().getType().equals(PieceType.ROOK) && "move".equals(p.getState())) {
                    foundMoving = true;
                    assert p.getPixelX() > 0.0 : "Moving rook should not remain at source pixelX";
                    assert p.getPixelX() < 200.0 : "Moving rook should not already be at destination pixelX";
                    break;
                }
            }

            assert foundMoving : "Expected one moving rook snapshot";
            System.out.println("[PASS] Test 2: interpolated moving piece snapshot");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 2: " + e.getMessage());
        }

        System.out.println("\n=== VisualSnapshotFactory Tests: " + passed + "/" + total + " passed ===");
    }
}