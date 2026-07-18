package ui;

import board.Board;
import board.Controller;
import engine.GameEngine;
import engine.GameEngineImpl;
import engine.GameSnapshot;
import models.Piece;
import models.PieceType;
import realtime.AirborneJump;
import realtime.Motion;
import rules.MoveValidation;
import rules.RuleEngine;
import rules.StandardRuleEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Produces renderer-ready visual snapshots from engine state.
 * Keeps visual state transitions outside renderer and game logic.
 */
public class VisualSnapshotFactory {
    private final GameEngine engine;
    private final Controller controller;
    private final int cellSize;
    private final SpriteAssetCatalog assets;
    private final RuleEngine moveHintRuleEngine;

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

    public VisualSnapshotFactory(
            GameEngine engine,
            Controller controller,
            int cellSize,
            SpriteAssetCatalog assets
    ) {
        this.engine = engine;
        this.controller = controller;
        this.cellSize = cellSize;
        this.assets = assets;
        if (engine instanceof GameEngineImpl) {
            this.moveHintRuleEngine = new StandardRuleEngine(((GameEngineImpl) engine).getConfig());
        } else {
            this.moveHintRuleEngine = null;
        }
    }

    public VisualSnapshot build(long uiTimeMs) {
        GameSnapshot snapshot = engine.snapshot();
        Board board = snapshot.getBoard();

        List<Motion> motions = engine.getActiveMotions();
        AirborneJump airborne = engine.getAirborneJump();

        updateVisualStateTracking(uiTimeMs, motions, airborne);

        Map<String, Motion> movingByDest = new HashMap<>();
        Map<String, Motion> movingBySource = new HashMap<>();
        for (Motion motion : motions) {
            movingByDest.put(squareKey(motion.getDestRow(), motion.getDestCol()), motion);
            movingBySource.put(squareKey(motion.getSrcRow(), motion.getSrcCol()), motion);
        }

        List<PieceVisualSnapshot> pieces = new ArrayList<>();

        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece == null) continue;

                Motion fromHere = movingBySource.get(squareKey(row, col));
                if (fromHere != null && piece == fromHere.getPiece()) continue;
                if (airborne != null && airborne.getRow() == row && airborne.getCol() == col) continue;

                String pieceKey = pieceKeyFor(piece);
                String state = stateForSquare(row, col, pieceKey);
                pieces.add(new PieceVisualSnapshot(
                        piece,
                        col * (double) cellSize,
                        row * (double) cellSize,
                        state,
                        -1.0,
                        row,
                        col
                ));
            }
        }

        for (Motion motion : motions) {
            double p = motion.getProgress();
            double srcX = motion.getSrcCol() * (double) cellSize;
            double srcY = motion.getSrcRow() * (double) cellSize;
            double dstX = motion.getDestCol() * (double) cellSize;
            double dstY = motion.getDestRow() * (double) cellSize;
            pieces.add(new PieceVisualSnapshot(
                    motion.getPiece(),
                    srcX + (dstX - srcX) * p,
                    srcY + (dstY - srcY) * p,
                    "move",
                    p,
                    -1,
                    -1
            ));
        }

        if (airborne != null) {
            double p = airborne.getProgress();
            double arc = 1.0 - (4.0 * (p - 0.5) * (p - 0.5));
            if (arc < 0.0) arc = 0.0;
            double lift = cellSize * 0.35 * arc;
            pieces.add(new PieceVisualSnapshot(
                    airborne.getPiece(),
                    airborne.getCol() * (double) cellSize,
                    airborne.getRow() * (double) cellSize - lift,
                    "jump",
                    p,
                    -1,
                    -1
            ));
        }

        return new VisualSnapshot(
                board.getWidth(),
                board.getHeight(),
                controller.getSelectedRow(),
                controller.getSelectedCol(),
                engine.isGameOver(),
                pieces,
                computeLegalDestinations(board, motions)
        );
    }

    private List<BoardCell> computeLegalDestinations(Board board, List<Motion> motions) {
        int srcRow = controller.getSelectedRow();
        int srcCol = controller.getSelectedCol();
        if (srcRow < 0 || srcCol < 0) return List.of();
        if (!board.isValid(srcRow, srcCol)) return List.of();
        if (board.getPieceAt(srcRow, srcCol) == null) return List.of();
        if (moveHintRuleEngine == null) return List.of();

        boolean isIncomingSource = false;
        for (Motion motion : motions) {
            if (motion.getDestRow() == srcRow && motion.getDestCol() == srcCol) {
                isIncomingSource = true;
                break;
            }
        }

        boolean motionInProgress = !motions.isEmpty();
        if (motionInProgress && !isIncomingSource) return List.of();

        List<BoardCell> result = new ArrayList<>();
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                if (row == srcRow && col == srcCol) continue;

                MoveValidation validation = moveHintRuleEngine.validateMove(board, srcRow, srcCol, row, col);
                if (validation.isValid()) {
                    result.add(new BoardCell(row, col));
                }
            }
        }
        return result;
    }

    private void updateVisualStateTracking(long uiTimeMs, List<Motion> activeMotions, AirborneJump airborne) {
        advanceSquareVisualStates(uiTimeMs);

        Set<String> currentDestKeys = new HashSet<>();
        for (Motion motion : activeMotions) {
            currentDestKeys.add(squareKey(motion.getDestRow(), motion.getDestCol()));
        }

        for (String previousDest : prevActiveDestKeys) {
            if (!currentDestKeys.contains(previousDest)) {
                startNextVisualState(uiTimeMs, previousDest, "move");
            }
        }
        prevActiveDestKeys.clear();
        prevActiveDestKeys.addAll(currentDestKeys);

        String currentAirborneKey = airborne == null ? null : squareKey(airborne.getRow(), airborne.getCol());
        if (prevAirborneKey != null && currentAirborneKey == null) {
            startNextVisualState(uiTimeMs, prevAirborneKey, "jump");
        }
        prevAirborneKey = currentAirborneKey;
    }

    private void advanceSquareVisualStates(long uiTimeMs) {
        List<String> keys = new ArrayList<>(squareVisualStates.keySet());
        for (String key : keys) {
            VisualState vs = squareVisualStates.get(key);
            if (vs == null || uiTimeMs < vs.untilMs) continue;
            startNextVisualState(uiTimeMs, key, vs.state);
        }
    }

    private void startNextVisualState(long uiTimeMs, String key, String fromState) {
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

        Piece piece = engine.snapshot().getBoard().getPieceAt(row, col);
        if (piece == null) {
            squareVisualStates.remove(key);
            return;
        }

        String pieceKey = pieceKeyFor(piece);
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

        List<java.awt.image.BufferedImage> frames = assets.getFrames(pieceKey, stateKey);
        int frameCount = frames == null ? 1 : Math.max(1, frames.size());
        int fps = fpsFor(pieceKey, stateKey);
        return Math.max(120L, (long) Math.ceil(1000.0 * frameCount / Math.max(1, fps)));
    }

    private int fpsFor(String pieceKey, String stateKey) {
        SpriteStateConfig cfg = assets.getStateConfig(pieceKey, stateKey);
        if (cfg == null || cfg.getFramesPerSec() <= 0) return 8;
        return cfg.getFramesPerSec();
    }

    private SpriteStateConfig stateConfigFor(String pieceKey, String stateKey) {
        return assets.getStateConfig(pieceKey, stateKey);
    }

    private String stateForSquare(int row, int col, String pieceKey) {
        VisualState vs = squareVisualStates.get(squareKey(row, col));
        if (vs == null) return "idle";
        if (!assets.hasState(pieceKey, vs.state)) return "idle";
        return vs.state;
    }

    private String squareKey(int row, int col) {
        return row + ":" + col;
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