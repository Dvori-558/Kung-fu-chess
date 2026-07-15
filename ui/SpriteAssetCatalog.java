package ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and serves sprite frames/configs by piece key and state.
 * Keeps asset concerns separate from rendering and snapshot logic.
 */
public class SpriteAssetCatalog {
    private final Map<String, Map<String, List<BufferedImage>>> spriteStates = new HashMap<>();
    private final Map<String, Map<String, SpriteStateConfig>> stateConfigs = new HashMap<>();
    private boolean spritesLoaded = false;

    public SpriteAssetCatalog() {
        loadSpriteAssets();
    }

    public boolean hasSprites() {
        return spritesLoaded;
    }

    public List<BufferedImage> getFrames(String pieceKey, String stateKey) {
        Map<String, List<BufferedImage>> byState = spriteStates.get(pieceKey);
        if (byState == null) {
            return null;
        }
        return byState.get(stateKey);
    }

    public boolean hasState(String pieceKey, String stateKey) {
        Map<String, List<BufferedImage>> byState = spriteStates.get(pieceKey);
        return byState != null && byState.containsKey(stateKey);
    }

    public SpriteStateConfig getStateConfig(String pieceKey, String stateKey) {
        Map<String, SpriteStateConfig> byState = stateConfigs.get(pieceKey);
        if (byState == null) {
            return null;
        }
        return byState.get(stateKey);
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
        if (pieceRoots == null) {
            return;
        }

        for (File pieceRoot : pieceRoots) {
            File[] pieceDirs = pieceRoot.listFiles(File::isDirectory);
            if (pieceDirs == null) continue;

            for (File pieceDir : pieceDirs) {
                File statesRoot = new File(pieceDir, "states");
                if (!statesRoot.exists() || !statesRoot.isDirectory()) continue;

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
        if (piece.length() != 2) return piece;
        return "" + piece.charAt(1) + piece.charAt(0);
    }
}