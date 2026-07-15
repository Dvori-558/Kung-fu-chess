package tests;

import models.Piece;
import models.PieceType;
import rules.GameConfig;
import ui.GameRenderer;
import ui.PieceVisualSnapshot;
import ui.SpriteAssetCatalog;
import ui.VisualSnapshot;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Smoke test for GameRenderer with a minimal visual snapshot.
 */
public class GameRendererSmokeTest {
    public static void main(String[] args) {
        int passed = 0;
        int total = 0;

        // Test 1: renderer can draw one static piece without throwing
        total++;
        try {
            GameConfig config = new GameConfig.Builder().buildStandardChess().build();
            SpriteAssetCatalog assets = new SpriteAssetCatalog();
            GameRenderer renderer = new GameRenderer(config, assets, 3, 3);

            Piece piece = new Piece(Piece.WHITE, PieceType.KING);
            PieceVisualSnapshot p = new PieceVisualSnapshot(
                    piece,
                    100.0,
                    100.0,
                    "idle",
                    -1.0,
                    1,
                    1
            );
            VisualSnapshot snapshot = new VisualSnapshot(
                    3,
                    3,
                    1,
                    1,
                    false,
                    List.of(p)
            );

            BufferedImage img = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            try {
                renderer.render(g, 0L, "", snapshot);
            } finally {
                g.dispose();
            }

            int rgb = img.getRGB(150, 150);
            assert rgb != 0 : "Expected non-empty drawn output around piece cell";
            System.out.println("[PASS] Test 1: renderer smoke draw");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
        }

        System.out.println("\n=== GameRenderer Smoke Tests: " + passed + "/" + total + " passed ===");
    }
}