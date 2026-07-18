package ui;

import models.Piece;
import models.PieceType;
import rules.GameConfig;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Renderer only: draws VisualSnapshot.
 */
public class GameRenderer {
    private final GameConfig config;
    private final SpriteAssetCatalog assets;
    private final int boardRows;
    private final int boardCols;
    private final long fallbackFrameTickMs = 120L;

    private final Map<BufferedImage, BufferedImage> monoSpriteCache = new HashMap<>();
    private final Map<BufferedImage, BufferedImage> whiteSilhouetteCache = new HashMap<>();
    private final Map<BufferedImage, BufferedImage> blackSilhouetteCache = new HashMap<>();

    private BufferedImage boardImage;

    public GameRenderer(GameConfig config, SpriteAssetCatalog assets, int boardCols, int boardRows) {
        this.config = config;
        this.assets = assets;
        this.boardCols = boardCols;
        this.boardRows = boardRows;

        loadBoardImage();
    }

    public Dimension getPreferredSize() {
        int cellSize = config.getPixelsPerCell();
        return new Dimension(cellSize * boardCols, cellSize * boardRows);
    }

    public void render(Graphics g, long uiTimeMs, String lastUiMessage, VisualSnapshot snapshot) {
        if (boardImage != null) {
            g.drawImage(boardImage, 0, 0, null);
        }

        drawLegalDestinations(g, snapshot);
        drawSelection(g, snapshot);
        drawPieces(g, snapshot, uiTimeMs);
        drawMessage(g, lastUiMessage, snapshot.isGameOver());
    }

    private void drawLegalDestinations(Graphics g, VisualSnapshot snapshot) {
        if (snapshot.getLegalDestinations() == null || snapshot.getLegalDestinations().isEmpty()) return;

        int cell = config.getPixelsPerCell();
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(119, 185, 73, 150));
        for (BoardCell cellPos : snapshot.getLegalDestinations()) {
            g2.fillRect(cellPos.getCol() * cell, cellPos.getRow() * cell, cell, cell);
        }
    }

    private void loadBoardImage() {
        int cellSize = config.getPixelsPerCell();
        int width = cellSize * boardCols;
        int height = cellSize * boardRows;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        for (int row = 0; row < boardRows; row++) {
            for (int col = 0; col < boardCols; col++) {
                g.setColor((row + col) % 2 == 0 ? Color.WHITE : Color.BLACK);
                g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
            }
        }
        g.dispose();
        boardImage = img;
    }

    private void drawSelection(Graphics g, VisualSnapshot snapshot) {
        int row = snapshot.getSelectedRow();
        int col = snapshot.getSelectedCol();
        if (row < 0 || col < 0) return;

        int cell = config.getPixelsPerCell();
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(255, 255, 0, 128));
        g2.fillRect(col * cell, row * cell, cell, cell);
    }

    private void drawPieces(Graphics g, VisualSnapshot snapshot, long uiTimeMs) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cellSize = config.getPixelsPerCell();
        for (PieceVisualSnapshot p : snapshot.getPieces()) {
            drawPiece(g2d, p, cellSize, uiTimeMs);
        }
    }

    private void drawPiece(Graphics2D g2d, PieceVisualSnapshot p, int cellSize, long uiTimeMs) {
        Piece piece = p.getPiece();
        int x = (int) Math.round(p.getPixelX());
        int y = (int) Math.round(p.getPixelY());

        BufferedImage sprite = getSpriteFrame(piece, p.getState(), p.getProgressHint(), uiTimeMs, p.getRow(), p.getCol());
        if (sprite != null) {
            BufferedImage mono = toMonochrome(sprite);
            BufferedImage outline = piece.getColor() == Piece.WHITE ? blackSilhouette(mono) : whiteSilhouette(mono);

            // Draw a 1px halo so monochrome pieces stay visible on same-color squares.
            g2d.drawImage(outline, x - 1, y, cellSize, cellSize, null);
            g2d.drawImage(outline, x + 1, y, cellSize, cellSize, null);
            g2d.drawImage(outline, x, y - 1, cellSize, cellSize, null);
            g2d.drawImage(outline, x, y + 1, cellSize, cellSize, null);

            g2d.drawImage(mono, x, y, cellSize, cellSize, null);
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
        g2d.drawString(label, centerX - fm.stringWidth(label) / 2, centerY + fm.getAscent() / 2);
    }

    private BufferedImage getSpriteFrame(Piece piece, String stateKey, double progressHint, long uiTimeMs, int row, int col) {
        if (!assets.hasSprites()) return null;

        String key = pieceKeyFor(piece);
        String effectiveState = (stateKey == null || stateKey.isEmpty()) ? "idle" : stateKey;
        java.util.List<BufferedImage> frames = assets.getFrames(key, effectiveState);
        if ((frames == null || frames.isEmpty()) && "move".equals(effectiveState)) frames = assets.getFrames(key, "jump");
        if ((frames == null || frames.isEmpty()) && !"idle".equals(effectiveState)) {
            frames = assets.getFrames(key, "idle");
            effectiveState = "idle";
        }
        if (frames == null || frames.isEmpty()) return null;

        if ("idle".equals(effectiveState)) {
            return frames.get(0);
        }

        int idx;
        if (progressHint >= 0.0 && ("move".equals(effectiveState) || "jump".equals(effectiveState))) {
            idx = Math.min(frames.size() - 1, Math.max(0, (int) Math.floor(progressHint * frames.size())));
        } else {
            int fps = fpsFor(key, effectiveState);
            int tickMs = Math.max(1, 1000 / Math.max(1, fps));
            long stablePhase = (row >= 0 && col >= 0) ? ((row * 31L + col * 17L) * 23L) : 0L;
            idx = (int) (((uiTimeMs + stablePhase) / tickMs) % frames.size());
        }

        return frames.get(idx);
    }

    private int fpsFor(String pieceKey, String stateKey) {
        SpriteStateConfig cfg = assets.getStateConfig(pieceKey, stateKey);
        if (cfg == null || cfg.getFramesPerSec() <= 0) return (int) (1000L / fallbackFrameTickMs);
        return cfg.getFramesPerSec();
    }

    private String pieceLabel(PieceType type) {
        if (type == PieceType.KNIGHT) return "N";
        return type.toString().substring(0, 1);
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

    private void drawMessage(Graphics g, String lastUiMessage, boolean gameOver) {
        if ((lastUiMessage == null || lastUiMessage.isEmpty()) && !gameOver) return;

        Graphics2D g2 = (Graphics2D) g;
        if (gameOver) {
            String title = "GAME OVER";
            String subtitle = (lastUiMessage != null && !lastUiMessage.isEmpty()) ? lastUiMessage : "No more moves";
            int w = boardCols * config.getPixelsPerCell();
            int h = boardRows * config.getPixelsPerCell();

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
        if (cached != null) return cached;

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
                int gC = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;
                int gray = (r * 30 + gC * 59 + b * 11) / 100;
                int bit = gray >= 128 ? 255 : 0;
                int mono = (a << 24) | (bit << 16) | (bit << 8) | bit;
                out.setRGB(x, y, mono);
            }
        }

        monoSpriteCache.put(src, out);
        return out;
    }

    private BufferedImage whiteSilhouette(BufferedImage src) {
        BufferedImage cached = whiteSilhouetteCache.get(src);
        if (cached != null) return cached;

        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int a = (src.getRGB(x, y) >>> 24) & 0xFF;
                if (a == 0) {
                    out.setRGB(x, y, 0);
                    continue;
                }
                out.setRGB(x, y, (a << 24) | 0x00FFFFFF);
            }
        }

        whiteSilhouetteCache.put(src, out);
        return out;
    }

    private BufferedImage blackSilhouette(BufferedImage src) {
        BufferedImage cached = blackSilhouetteCache.get(src);
        if (cached != null) return cached;

        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int a = (src.getRGB(x, y) >>> 24) & 0xFF;
                if (a == 0) {
                    out.setRGB(x, y, 0);
                    continue;
                }
                out.setRGB(x, y, (a << 24));
            }
        }

        blackSilhouetteCache.put(src, out);
        return out;
    }
}
