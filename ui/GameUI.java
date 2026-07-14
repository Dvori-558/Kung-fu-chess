package ui;

import board.Board;
import board.Controller;
import engine.GameEngine;
import engine.GameSnapshot;
import models.Piece;
import models.PieceType;
import realtime.Motion;
import realtime.AirborneJump;
import rules.GameConfig;
import utils.CoordinateConverter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.File;
import java.nio.file.Path;

public class GameUI extends JPanel {
    private final GameEngine engine;
    private final Controller controller;
    private final GameConfig config;
    private final CoordinateConverter converter;
    private final int boardRows;
    private final int boardCols;
    private final long frameTickMs = 120L;
    private final Map<String, Map<String, List<BufferedImage>>> spriteStates = new HashMap<>();
    private final Map<String, Map<String, SpriteStateConfig>> stateConfigs = new HashMap<>();
    
    private BufferedImage boardImage;
    private boolean spritesLoaded = false;
    private long uiTimeMs = 0L;
    private String lastUiMessage = "";
    private final Map<BufferedImage, BufferedImage> monoSpriteCache = new HashMap<>();
    private final Map<String, VisualState> squareVisualStates = new HashMap<>();
    private final Set<String> prevActiveDestKeys = new HashSet<>();
    private String prevAirborneKey = null;

    private static class VisualState {
        private final String state;
        private final long startMs;
        private final long untilMs;

        private VisualState(String state, long startMs, long untilMs) {
            this.state = state;
            this.startMs = startMs;
            this.untilMs = untilMs;
        }
    }

    public GameUI(GameEngine engine, Controller controller, GameConfig config) {
        this.engine = engine;
        this.controller = controller;
        this.config = config;
        this.converter = new CoordinateConverter(config.getPixelsPerCell());
        GameSnapshot snapshot = engine.snapshot();
        this.boardRows = snapshot.getBoard().getHeight();
        this.boardCols = snapshot.getBoard().getWidth();
        
        loadSpriteAssets();
        loadAssets();
        setupInput();
        
        System.out.println("UI Initialized with cell size: " + converter.getPixelsPerCell());
        
        Timer timer = new Timer(30, e -> {
            uiTimeMs += 30L;
            engine.pause(30);
            repaint();
        });
        timer.start();
    }

    private void loadSpriteAssets() {
        File root = new File(".github/CTD26");
        if (!root.exists()) {
            root = new File(".github/CTD26-main");
        }
        if (!root.exists()) {
            return;
        }

        File[] pieceRoots = root.listFiles(file -> file.isDirectory() && (file.getName().equals("pieces1") || file.getName().equals("pieces2")));
        if (pieceRoots == null) return;

        for (File pieceRoot : pieceRoots) {
            File[] pieceDirs = pieceRoot.listFiles(File::isDirectory);
            if (pieceDirs == null) continue;

            for (File pieceDir : pieceDirs) {
                File statesRoot = new File(pieceDir, "states");
                if (!statesRoot.exists() || !statesRoot.isDirectory()) {
                    continue;
                }

                File[] stateDirs = statesRoot.listFiles(File::isDirectory);
                if (stateDirs == null) continue;

                for (File stateDir : stateDirs) {
                    File graphicsDir = new File(stateDir, "sprites");
                    List<BufferedImage> frames = loadOrderedPngFrames(graphicsDir.exists() ? graphicsDir : stateDir);
                    if (frames.isEmpty()) continue;

                    String pieceKey = normalizePieceKey(pieceDir.getName());
                    String stateKey = stateDir.getName().toLowerCase();
                    spriteStates.computeIfAbsent(pieceKey, k -> new HashMap<>()).put(stateKey, frames);
                    Path cfgPath = new File(stateDir, "config.json").toPath();
                    SpriteStateConfig cfg = SpriteConfigLoader.load(cfgPath);
                    stateConfigs.computeIfAbsent(pieceKey, k -> new HashMap<>()).put(stateKey, cfg);
                    spritesLoaded = true;
                }
            }
        }
    }

    private List<BufferedImage> loadOrderedPngFrames(File dir) {
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) return List.of();

        List<File> ordered = new ArrayList<>();
        for (File f : files) ordered.add(f);
        ordered.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        List<BufferedImage> frames = new ArrayList<>();
        for (File f : ordered) {
            try {
                frames.add(new Img().read(f.getPath()).get());
            } catch (Exception ignored) {
            }
        }
        return frames;
    }

    private String normalizePieceKey(String pieceDirName) {
        String piece = pieceDirName.toUpperCase();
        if (piece.length() != 2) {
            return piece;
        }
        return "" + piece.charAt(1) + piece.charAt(0);
    }

    private void loadAssets() {
        try {
            boardImage = createBoardImage();
            System.out.println("Board created programmatically.");
        } catch (Exception e) {
            System.err.println("Error loading assets: " + e.getMessage());
        }
    }
    
    private BufferedImage createBoardImage() {
        int cellSize = config.getPixelsPerCell();
        int width = cellSize * boardCols;
        int height = cellSize * boardRows;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        
        for (int row = 0; row < boardRows; row++) {
            for (int col = 0; col < boardCols; col++) {
                g.setColor((row + col) % 2 == 0 ? Color.LIGHT_GRAY : Color.GRAY);
                g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
            }
        }
        g.dispose();
        return img;
    }

    private void setupInput() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (engine.isGameOver()) {
                    lastUiMessage = "Game over - no more moves";
                    return;
                }

                if (SwingUtilities.isLeftMouseButton(e)) {
                    controller.click(e.getX(), e.getY());
                    String reason = controller.getLastMoveReason();
                    if (reason != null) {
                        lastUiMessage = "Move rejected: " + reason;
                    } else {
                        lastUiMessage = "";
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    controller.jump(e.getX(), e.getY());
                    String reason = controller.getLastMoveReason();
                    if (reason != null) {
                        lastUiMessage = reason;
                    } else {
                        lastUiMessage = "";
                    }
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (boardImage != null) {
            g.drawImage(boardImage, 0, 0, null);
        }
        
        GameSnapshot snapshot = engine.snapshot();
        Board board = snapshot.getBoard();
        drawPieces(g, board);
        drawMotionOverlay(g);
        drawMessage(g);
    }

    @Override
    public Dimension getPreferredSize() {
        int cellSize = config.getPixelsPerCell();
        return new Dimension(cellSize * boardCols, cellSize * boardRows);
    }

    private void drawPieces(Graphics g, Board board) {
        int cellSize = config.getPixelsPerCell();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Map<String, Motion> movingByDest = activeMotionsByDest();
        AirborneJump airborne = engine.getAirborneJump();
        
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null) {
                    if (movingByDest.containsKey(squareKey(row, col))) {
                        continue;
                    }
                    if (airborne != null && airborne.getRow() == row && airborne.getCol() == col) {
                        continue;
                    }

                    int x = col * cellSize;
                    int y = row * cellSize;
                    
                    // Draw selection highlight
                    if (row == controller.getSelectedRow() && col == controller.getSelectedCol()) {
                        g.setColor(new Color(255, 255, 0, 128));
                        g.fillRect(x, y, cellSize, cellSize);
                    }

                    drawPiece(g2d, piece, x, y, cellSize, false, row, col, null, -1.0);
                }
            }
        }
    }

    private void drawMotionOverlay(Graphics g) {
        int cellSize = config.getPixelsPerCell();
        Graphics2D g2d = (Graphics2D) g;
        List<Motion> motions = engine.getActiveMotions();
        AirborneJump airborne = engine.getAirborneJump();

        updateVisualStateTracking(motions, airborne);

        for (Motion motion : motions) {
            drawMovingPiece(g2d, motion, cellSize);
        }
        if (airborne != null) {
            drawAirborneJump(g2d, airborne, cellSize);
        }
    }

    private void drawMovingPiece(Graphics2D g2d, Motion motion, int cellSize) {
        double p = motion.getProgress();
        int srcX = motion.getSrcCol() * cellSize;
        int srcY = motion.getSrcRow() * cellSize;
        int dstX = motion.getDestCol() * cellSize;
        int dstY = motion.getDestRow() * cellSize;
        int x = (int) Math.round(srcX + (dstX - srcX) * p);
        int y = (int) Math.round(srcY + (dstY - srcY) * p);
        drawPiece(g2d, motion.getPiece(), x, y, cellSize, true, -1, -1, "move", motion.getProgress());
    }

    private void drawAirborneJump(Graphics2D g2d, AirborneJump airborne, int cellSize) {
        int baseX = airborne.getCol() * cellSize;
        int baseY = airborne.getRow() * cellSize;
        double p = airborne.getProgress();
        double arc = 1.0 - (4.0 * (p - 0.5) * (p - 0.5));
        if (arc < 0.0) arc = 0.0;
        int lift = (int) Math.round(cellSize * 0.35 * arc);
        drawPiece(g2d, airborne.getPiece(), baseX, baseY - lift, cellSize, true, -1, -1, "jump", airborne.getProgress());
    }

    private void drawPiece(Graphics2D g2d, Piece piece, int x, int y, int cellSize, boolean moving, int row, int col, String forcedState, double progressHint) {
        BufferedImage sprite = getSpriteFrame(piece, moving, row, col, forcedState, progressHint);
        if (sprite != null) {
            g2d.drawImage(toMonochrome(sprite), x, y, cellSize, cellSize, null);
            return;
        }

        int radius = cellSize / 2 - 8;
        int centerX = x + cellSize / 2;
        int centerY = y + cellSize / 2;

        g2d.setColor(piece.getColor() == Piece.WHITE ? new Color(248, 248, 248) : new Color(20, 20, 20));
        g2d.fillOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);

        g2d.setColor(piece.getColor() == Piece.WHITE ? Color.BLACK : Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        String label = pieceLabel(piece.getType());
        FontMetrics fm = g2d.getFontMetrics();
        int tx = centerX - fm.stringWidth(label) / 2;
        int ty = centerY + fm.getAscent() / 2;
        g2d.drawString(label, tx, ty);
    }

    private String pieceLabel(PieceType type) {
        if (type == PieceType.KNIGHT) return "N";
        return type.toString().substring(0, 1);
    }

    private BufferedImage getSpriteFrame(Piece piece, boolean moving, int row, int col, String forcedState, double progressHint) {
        if (!spritesLoaded) {
            return null;
        }

        String key = pieceKeyFor(piece);
        Map<String, List<BufferedImage>> states = spriteStates.get(key);
        if (states == null || states.isEmpty()) {
            return null;
        }

        String stateKey = forcedState;
        if (stateKey == null || stateKey.isEmpty()) {
            if (moving) {
                stateKey = "move";
            } else {
                stateKey = stateForSquare(row, col, key);
            }
        }

        List<BufferedImage> frames = states.get(stateKey);
        if ((frames == null || frames.isEmpty()) && stateKey.equals("move")) {
            frames = states.get("jump");
        }
        if ((frames == null || frames.isEmpty()) && !stateKey.equals("idle")) {
            frames = states.get("idle");
            stateKey = "idle";
        }
        if (frames == null || frames.isEmpty()) {
            return null;
        }

        SpriteStateConfig cfg = stateConfigFor(key, stateKey);
        if (!moving && "idle".equals(stateKey) && (cfg == null || !cfg.isLoop())) {
            return frames.get(0);
        }

        int idx;
        if (progressHint >= 0.0) {
            idx = Math.min(frames.size() - 1, Math.max(0, (int) Math.floor(progressHint * frames.size())));
        } else {
            int fps = fpsFor(key, stateKey);
            int tickMs = Math.max(1, 1000 / Math.max(1, fps));
            VisualState active = row >= 0 && col >= 0 ? squareVisualStates.get(squareKey(row, col)) : null;
            long baseTime = (active != null && active.state.equals(stateKey)) ? active.startMs : uiTimeMs;
            idx = (int) (((uiTimeMs - baseTime) / tickMs) % frames.size());
            if (idx < 0) idx = 0;
        }
        return frames.get(idx);
    }

    private int fpsFor(String pieceKey, String stateKey) {
        Map<String, SpriteStateConfig> byState = stateConfigs.get(pieceKey);
        if (byState == null) {
            return (int) (1000L / frameTickMs);
        }
        SpriteStateConfig cfg = byState.get(stateKey);
        if (cfg == null || cfg.getFramesPerSec() <= 0) {
            return (int) (1000L / frameTickMs);
        }
        return cfg.getFramesPerSec();
    }

    private void updateVisualStateTracking(List<Motion> activeMotions, AirborneJump airborne) {
        advanceSquareVisualStates();

        Set<String> currentDestKeys = new HashSet<>();
        for (Motion motion : activeMotions) {
            currentDestKeys.add(squareKey(motion.getDestRow(), motion.getDestCol()));
        }

        for (String previousDest : prevActiveDestKeys) {
            if (!currentDestKeys.contains(previousDest)) {
                startNextVisualState(previousDest, "move");
            }
        }
        prevActiveDestKeys.clear();
        prevActiveDestKeys.addAll(currentDestKeys);

        String currentAirborneKey = airborne == null ? null : squareKey(airborne.getRow(), airborne.getCol());
        if (prevAirborneKey != null && currentAirborneKey == null) {
            startNextVisualState(prevAirborneKey, "jump");
        }
        prevAirborneKey = currentAirborneKey;
    }

    private void advanceSquareVisualStates() {
        List<String> keys = new ArrayList<>(squareVisualStates.keySet());
        for (String key : keys) {
            VisualState vs = squareVisualStates.get(key);
            if (vs == null || uiTimeMs < vs.untilMs) {
                continue;
            }
            startNextVisualState(key, vs.state);
        }
    }

    private void startNextVisualState(String key, String fromState) {
        String[] parts = key.split(":");
        if (parts.length != 2) {
            squareVisualStates.remove(key);
            return;
        }
        int row;
        int col;
        try {
            row = Integer.parseInt(parts[0]);
            col = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            squareVisualStates.remove(key);
            return;
        }

        Piece p = engine.snapshot().getBoard().getPieceAt(row, col);
        if (p == null) {
            squareVisualStates.remove(key);
            return;
        }

        String pieceKey = pieceKeyFor(p);
        String nextState = nextStateFor(pieceKey, fromState);
        if (nextState == null || nextState.isEmpty() || "idle".equals(nextState)) {
            squareVisualStates.remove(key);
            return;
        }

        long duration = stateDurationMs(pieceKey, nextState);
        if (duration <= 0) {
            squareVisualStates.remove(key);
            return;
        }

        squareVisualStates.put(key, new VisualState(nextState, uiTimeMs, uiTimeMs + duration));
    }

    private String nextStateFor(String pieceKey, String state) {
        SpriteStateConfig cfg = stateConfigFor(pieceKey, state);
        if (cfg == null) {
            if ("move".equals(state) || "jump".equals(state)) return "short_rest";
            if ("short_rest".equals(state) || "long_rest".equals(state)) return "idle";
            return "idle";
        }
        String next = cfg.getNextState();
        if (next == null || next.isEmpty()) return "idle";
        return next.toLowerCase();
    }

    private long stateDurationMs(String pieceKey, String stateKey) {
        SpriteStateConfig cfg = stateConfigFor(pieceKey, stateKey);
        if (cfg != null && cfg.getSpeedCellsPerSec() > 0.0) {
            return Math.max(120L, Math.round((1.0 / cfg.getSpeedCellsPerSec()) * 1000.0));
        }

        List<BufferedImage> frames = spriteStates.getOrDefault(pieceKey, Map.of()).get(stateKey);
        int frameCount = frames == null ? 1 : Math.max(1, frames.size());
        int fps = fpsFor(pieceKey, stateKey);
        return Math.max(120L, (long) Math.ceil(1000.0 * frameCount / Math.max(1, fps)));
    }

    private SpriteStateConfig stateConfigFor(String pieceKey, String stateKey) {
        Map<String, SpriteStateConfig> byState = stateConfigs.get(pieceKey);
        if (byState == null) {
            return null;
        }
        return byState.get(stateKey);
    }

    private String stateForSquare(int row, int col, String pieceKey) {
        VisualState vs = squareVisualStates.get(squareKey(row, col));
        if (vs == null) {
            return "idle";
        }
        if (!spriteStates.getOrDefault(pieceKey, Map.of()).containsKey(vs.state)) {
            return "idle";
        }
        return vs.state;
    }

    private Map<String, Motion> activeMotionsByDest() {
        Map<String, Motion> map = new HashMap<>();
        for (Motion motion : engine.getActiveMotions()) {
            map.put(squareKey(motion.getDestRow(), motion.getDestCol()), motion);
        }
        return map;
    }

    private String squareKey(int row, int col) {
        return row + ":" + col;
    }

    private String pieceKeyFor(Piece piece) {
        char color = piece.getColor() == Piece.WHITE ? 'W' : 'B';
        char type;
        if (piece.getType() == PieceType.KNIGHT) type = 'N';
        else if (piece.getType() == PieceType.BISHOP) type = 'B';
        else if (piece.getType() == PieceType.ROOK) type = 'R';
        else if (piece.getType() == PieceType.QUEEN) type = 'Q';
        else if (piece.getType() == PieceType.KING) type = 'K';
        else type = 'P';
        return "" + color + type;
    }

    private void drawMessage(Graphics g) {
        boolean gameOver = engine.isGameOver();
        if ((lastUiMessage == null || lastUiMessage.isEmpty()) && !gameOver) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        if (gameOver) {
            String title = "GAME OVER";
            String subtitle = "No more moves";
            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(0, 0, 0, 170));
            g2.fillRect(0, 0, w, h);

            g2.setFont(new Font("SansSerif", Font.BOLD, 56));
            FontMetrics fmTitle = g2.getFontMetrics();
            int tx = (w - fmTitle.stringWidth(title)) / 2;
            int ty = h / 2 - 10;
            g2.setColor(Color.WHITE);
            g2.drawString(title, tx, ty);

            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            FontMetrics fmSub = g2.getFontMetrics();
            int sx = (w - fmSub.stringWidth(subtitle)) / 2;
            int sy = ty + 36;
            g2.drawString(subtitle, sx, sy);
            return;
        }

        g2.setColor(new Color(30, 30, 30, 180));
        g2.fillRoundRect(8, 8, Math.max(220, g2.getFontMetrics().stringWidth(lastUiMessage) + 20), 28, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString(lastUiMessage, 18, 27);
    }

    private BufferedImage toMonochrome(BufferedImage src) {
        BufferedImage cached = monoSpriteCache.get(src);
        if (cached != null) {
            return cached;
        }

        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = src.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {
                    out.setRGB(x, y, 0);
                    continue;
                }

                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;
                int gray = (r * 30 + g * 59 + b * 11) / 100;
                int bit = gray >= 128 ? 255 : 0;
                int mono = (a << 24) | (bit << 16) | (bit << 8) | bit;
                out.setRGB(x, y, mono);
            }
        }

        monoSpriteCache.put(src, out);
        return out;
    }

    public static void launch(GameEngine engine, Controller controller, GameConfig config) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kung-fu Chess");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            GameUI ui = new GameUI(engine, controller, config);
            frame.add(ui);
            frame.pack();
            if (ui.boardImage == null) {
                frame.setSize(800, 800);
            }
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}