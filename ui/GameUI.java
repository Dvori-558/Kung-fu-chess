package ui;

import board.Controller;
import engine.GameEngine;
import rules.GameConfig;
import utils.CoordinateConverter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Swing view adapter: routes mouse input to Controller and delegates drawing to GameRenderer.
 */
public class GameUI extends JPanel {
    private final GameEngine engine;
    private final Controller controller;
    private final CoordinateConverter converter;
    private final GameRenderer renderer;
    private final VisualSnapshotFactory visualSnapshotFactory;

    private long uiTimeMs = 0L;
    private String lastUiMessage = "";

    public GameUI(GameEngine engine, Controller controller, GameConfig config) {
        this.engine = engine;
        this.controller = controller;
        this.converter = new CoordinateConverter(config.getPixelsPerCell());
        int boardCols = engine.snapshot().getBoard().getWidth();
        int boardRows = engine.snapshot().getBoard().getHeight();
        SpriteAssetCatalog assets = new SpriteAssetCatalog();
        this.renderer = new GameRenderer(config, assets, boardCols, boardRows);
        this.visualSnapshotFactory = new VisualSnapshotFactory(
                engine,
                controller,
                config.getPixelsPerCell(),
                assets
        );

        setupInput();

        Timer timer = new Timer(30, e -> {
            uiTimeMs += 30L;
            engine.pause(30);
            repaint();
        });
        timer.start();
    }

    private void setupInput() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (engine.isGameOver()) {
                    lastUiMessage = "\u05d4\u05de\u05e9\u05d7\u05e7\u0020\u05d4\u05e1\u05ea\u05d9\u05d9\u05dd\u0020\u002d\u0020\u05dc\u05d0\u0020\u05e0\u05d9\u05ea\u05df\u0020\u05dc\u05d1\u05e6\u05e2\u0020\u05de\u05d4\u05dc\u05db\u05d9\u05dd\u0020\u05e0\u05d5\u05e1\u05e4\u05d9\u05dd";
                    return;
                }

                if (SwingUtilities.isLeftMouseButton(e)) {
                    controller.click(e.getX(), e.getY());
                    String reason = controller.getLastMoveReason();
                    lastUiMessage = toUiMessage(reason);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    controller.jump(e.getX(), e.getY());
                    String reason = controller.getLastMoveReason();
                    lastUiMessage = toUiMessage(reason);
                }
            }
        });
    }

    private String toUiMessage(String reason) {
        if (reason == null || reason.isEmpty()) {
            return "";
        }

        String key = reason.trim().toLowerCase();
        if (key.startsWith("jump rejected: outside board")) return "\u05dc\u05d0\u0020\u05e0\u05d9\u05ea\u05df\u0020\u05dc\u05e7\u05e4\u05d5\u05e5\u0020\u05de\u05d7\u05d5\u05e5\u0020\u05dc\u05d2\u05d1\u05d5\u05dc\u05d5\u05ea\u0020\u05d4\u05dc\u05d5\u05d7";
        if (key.startsWith("jump rejected")) return "\u05d4\u05e7\u05e4\u05d9\u05e6\u05d4\u0020\u05e0\u05d3\u05d7\u05ea\u05d4";
        if (key.equals("outside_board")) return "\u05d4\u05de\u05d4\u05dc\u05da\u0020\u05de\u05d7\u05d5\u05e5\u0020\u05dc\u05d2\u05d1\u05d5\u05dc\u05d5\u05ea\u0020\u05d4\u05dc\u05d5\u05d7";
        if (key.equals("empty_source")) return "\u05d0\u05d9\u05df\u0020\u05db\u05dc\u05d9\u0020\u05d1\u05de\u05e9\u05d1\u05e6\u05ea\u0020\u05e9\u05e0\u05d1\u05d7\u05e8\u05d4";
        if (key.equals("friendly_destination")) return "\u05dc\u05d0\u0020\u05e0\u05d9\u05ea\u05df\u0020\u05dc\u05d0\u05db\u05d5\u05dc\u0020\u05db\u05dc\u05d9\u0020\u05de\u05d0\u05d5\u05ea\u05d5\u0020\u05e6\u05d1\u05e2";
        if (key.equals("illegal_piece_move")) return "\u05d4\u05de\u05d4\u05dc\u05da\u0020\u05dc\u05d0\u0020\u05d7\u05d5\u05e7\u05d9\u0020\u05e2\u05d1\u05d5\u05e8\u0020\u05d4\u05db\u05dc\u05d9\u0020\u05e9\u05e0\u05d1\u05d7\u05e8";
        if (key.equals("motion_in_progress")) return "\u05d9\u05e9\u0020\u05de\u05d4\u05dc\u05da\u0020\u05e4\u05e2\u05d9\u05dc\u0020\u05db\u05e8\u05d2\u05e2\u0020\u002d\u0020\u05e0\u05e1\u05d9\u0020\u05e9\u05d5\u05d1\u0020\u05d1\u05e2\u05d5\u05d3\u0020\u05e8\u05d2\u05e2";
        if (key.equals("game_over")) return "\u05d4\u05de\u05e9\u05d7\u05e7\u0020\u05d4\u05e1\u05ea\u05d9\u05d9\u05dd";
        if (key.equals("ok")) return "";

        return "\u05d4\u05de\u05d4\u05dc\u05da\u0020\u05e0\u05d3\u05d7\u05d4";
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        VisualSnapshot snapshot = visualSnapshotFactory.build(uiTimeMs);
        renderer.render(g, uiTimeMs, lastUiMessage, snapshot);
    }

    @Override
    public Dimension getPreferredSize() {
        return renderer.getPreferredSize();
    }

    public static void launch(GameEngine engine, Controller controller, GameConfig config) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kung-fu Chess");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            GameUI ui = new GameUI(engine, controller, config);
            frame.add(ui);
            frame.pack();
            if (ui.getPreferredSize().width <= 0 || ui.getPreferredSize().height <= 0) {
                frame.setSize(800, 800);
            }
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

}
