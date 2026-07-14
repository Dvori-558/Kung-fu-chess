package engine;

import board.Board;
import realtime.RealTimeArbiter;
import realtime.Motion;
import realtime.AirborneJump;
import rules.GameConfig;
import rules.RuleEngine;
import rules.MoveValidation;
import rules.StandardRuleEngine;
import models.Piece;
import models.PieceType;
import ui.SpriteConfigLoader;
import ui.SpriteStateConfig;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/** Coordinates validation, motion, and game state guards. */
public class GameEngineImpl implements GameEngine {
    private final Board board;
    private final GameConfig config;
    private final RuleEngine ruleEngine;
    private final RealTimeArbiter realTimeArbiter;
    private final WinManager winManager;
    private final Map<String, Map<String, SpriteStateConfig>> pieceStateConfigs = new HashMap<>();

    public GameEngineImpl(Board board, GameConfig config) {
        this.board = board;
        // Store config for potential future use; currently delegated to components.
        // If not needed, it can be removed in a future refactor.
        this.config = config;
        this.ruleEngine = new StandardRuleEngine(config);
        this.winManager = new WinManager(config.getWinCondition());
        this.realTimeArbiter = new RealTimeArbiter(board, config.getWinCondition(), config.getPromotionRule());
        loadSpriteConfigs();
    }

    @Override
    public MoveResult requestMove(int srcRow, int srcCol, int destRow, int destCol) {
        if (winManager.isGameOver()) {
            return MoveResult.rejected(MoveResult.GAME_OVER);
        }

        if (realTimeArbiter.hasActiveMotion()) {
            return MoveResult.rejected(MoveResult.MOTION_IN_PROGRESS);
        }

        MoveValidation validation = ruleEngine.validateMove(board, srcRow, srcCol, destRow, destCol);

        if (!validation.isValid()) {
            return MoveResult.rejected(validation.getReason());
        }

        Piece piece = board.getPieceAt(srcRow, srcCol);
        long durationMs = calculateMotionDuration(piece, srcRow, srcCol, destRow, destCol);
        realTimeArbiter.startMotion(piece, srcRow, srcCol, destRow, destCol, durationMs);

        return MoveResult.accepted();
    }

    @Override
    public boolean requestJump(int row, int col) {
        if (winManager.isGameOver()) {
            return false;
        }
        Piece piece = board.getPieceAt(row, col);
        long jumpDuration = calculateJumpDuration(piece);
        return realTimeArbiter.startJump(row, col, jumpDuration);
    }

    @Override
    public void pause(long durationMs) {
        realTimeArbiter.advanceTime(durationMs);
    }

    @Override
    public boolean hasActiveMotion() {
        return realTimeArbiter.hasActiveMotion();
    }

    @Override
    public List<Motion> getActiveMotions() {
        return realTimeArbiter.getActiveMotions();
    }

    @Override
    public AirborneJump getAirborneJump() {
        return realTimeArbiter.getAirborneJump();
    }

    @Override
    public boolean isGameOver() {
        return winManager.isGameOver();
    }

    @Override
    public GameSnapshot snapshot() {
        return new GameSnapshot(board, winManager.isGameOver());
    }

    /** Provides access to game configuration for introspection. */
    public GameConfig getConfig() {
        return config;
    }

    /** Computes move duration from board distance. */
    private long calculateMotionDuration(Piece piece, int srcRow, int srcCol, int destRow, int destCol) {
        int rowDist = Math.abs(destRow - srcRow);
        int colDist = Math.abs(destCol - srcCol);
        int cells = Math.max(rowDist, colDist);
        if (cells == 0) cells = 1;

        double speed = speedFor(piece, "move");
        if (speed > 0.0) {
            return Math.max(1L, Math.round((cells / speed) * 1000.0));
        }

        return cells * (long) config.getMoveDurationMs();
    }

    private long calculateJumpDuration(Piece piece) {
        double speed = speedFor(piece, "jump");
        if (speed > 0.0) {
            return Math.max(1L, Math.round((1.0 / speed) * 1000.0));
        }
        return 1000L;
    }

    private void loadSpriteConfigs() {
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

                String pieceKey = normalizePieceKey(pieceDir.getName());
                File[] stateDirs = statesRoot.listFiles(File::isDirectory);
                if (stateDirs == null) continue;

                for (File stateDir : stateDirs) {
                    String state = stateDir.getName().toLowerCase();
                    SpriteStateConfig cfg = SpriteConfigLoader.load(new File(stateDir, "config.json").toPath());
                    pieceStateConfigs.computeIfAbsent(pieceKey, k -> new HashMap<>()).put(state, cfg);
                }
            }
        }
    }

    private String normalizePieceKey(String pieceDirName) {
        String piece = pieceDirName.toUpperCase();
        if (piece.length() != 2) {
            return piece;
        }
        return "" + piece.charAt(1) + piece.charAt(0);
    }

    private double speedFor(Piece piece, String state) {
        if (piece == null) {
            return 0.0;
        }
        Map<String, SpriteStateConfig> byState = pieceStateConfigs.get(pieceKeyFor(piece));
        if (byState == null) {
            return 0.0;
        }
        SpriteStateConfig cfg = byState.get(state);
        if (cfg == null) {
            return 0.0;
        }
        return Math.max(0.0, cfg.getSpeedCellsPerSec());
    }

    private String pieceKeyFor(Piece piece) {
        char color = piece.getColor() == Piece.WHITE ? 'W' : 'B';
        char type;
        if (piece.getType().equals(PieceType.KNIGHT)) type = 'N';
        else if (piece.getType().equals(PieceType.BISHOP)) type = 'B';
        else if (piece.getType().equals(PieceType.ROOK)) type = 'R';
        else if (piece.getType().equals(PieceType.QUEEN)) type = 'Q';
        else if (piece.getType().equals(PieceType.KING)) type = 'K';
        else type = 'P';
        return "" + color + type;
    }
}
