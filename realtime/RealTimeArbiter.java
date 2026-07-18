package realtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import board.Board;
import rules.PromotionRule;
import rules.WinCondition;
import models.Piece;
import models.PieceType;

/** Advances active motions and resolves arrivals on the board. */
public class RealTimeArbiter {
    private final Board board;
    private final WinCondition winCondition;
    private final PromotionRule promotionRule;
    private final List<Motion> activeMotions = new ArrayList<>();
    private final Map<String, Long> restUntilBySquare = new HashMap<>();
    private long simulatedTimeMs = 0L;
    private Piece airbornePiece;
    private int airborneRow = -1;
    private int airborneCol = -1;
    private int airbornePrevRow = -1;
    private int airbornePrevCol = -1;
    private long airborneRemainingMs = 0L;
    private long airborneTotalMs = 0L;
    private int lastArrivedSrcRow = -1;
    private int lastArrivedSrcCol = -1;
    private int lastArrivedDestRow = -1;
    private int lastArrivedDestCol = -1;

    public RealTimeArbiter(Board board, WinCondition winCondition, PromotionRule promotionRule) {
        this.board = board;
        this.winCondition = winCondition;
        this.promotionRule = promotionRule;
    }

    /** Registers a new motion. */
    public void startMotion(Piece piece, int srcRow, int srcCol, int destRow, int destCol, long durationMs) {
        Motion motion = new Motion(piece, srcRow, srcCol, destRow, destCol, durationMs);
        activeMotions.add(motion);
    }

    /**
     * Starts an airborne jump from the provided board cell.
     */
    public boolean startJump(int row, int col, long jumpDurationMs) {
        if (airbornePiece != null) {
            return false;
        }
        if (hasActiveMotion() && !isIncomingDestination(row, col)) {
            return false;
        }
        if (!board.isValid(row, col)) {
            return false;
        }

        Piece piece = board.getPieceAt(row, col);
        if (piece == null) {
            return false;
        }

        airbornePiece = piece;
        airborneRow = row;
        airborneCol = col;
        airbornePrevRow = row;
        airbornePrevCol = col;
        airborneTotalMs = Math.max(1L, jumpDurationMs);
        airborneRemainingMs = airborneTotalMs;
        board.setPieceAt(row, col, null);
        return true;
    }

    /** Advances time and resolves completed motions. */
    public void advanceTime(long deltaMs) {
        simulatedTimeMs += Math.max(0L, deltaMs);

        for (Motion motion : activeMotions) {
            motion.advance(deltaMs);
        }

        List<Motion> arrived = new ArrayList<>();
        for (Motion motion : activeMotions) {
            if (motion.hasArrived()) {
                arrived.add(motion);
            }
        }

        for (Motion motion : arrived) {
            resolveArrival(motion);
        }
        activeMotions.removeAll(arrived);

        if (airbornePiece != null) {
            airborneRemainingMs -= deltaMs;
            if (airborneRemainingMs <= 0) {
                resolveJumpLanding();
                clearAirborne();
            }
        }
    }

    private void resolveJumpLanding() {
        Piece target = board.getPieceAt(airborneRow, airborneCol);
        if (target == null) {
            board.setPieceAt(airborneRow, airborneCol, airbornePiece);
            startRestAt(airborneRow, airborneCol, 600L);
            return;
        }

        if (target.getColor() != airbornePiece.getColor()) {
            if (target.getType() == PieceType.KING) {
                winCondition.recordKingCapture(airbornePiece.getColor());
            }
            board.setPieceAt(airborneRow, airborneCol, airbornePiece);
            startRestAt(airborneRow, airborneCol, 600L);
            return;
        }

        if (lastArrivedDestRow == airborneRow && lastArrivedDestCol == airborneCol
                && board.isValid(lastArrivedSrcRow, lastArrivedSrcCol)
                && board.getPieceAt(lastArrivedSrcRow, lastArrivedSrcCol) == null) {
            board.setPieceAt(lastArrivedSrcRow, lastArrivedSrcCol, target);
            board.setPieceAt(airborneRow, airborneCol, airbornePiece);
            startRestAt(airborneRow, airborneCol, 600L);
            return;
        }

        if (board.isValid(airbornePrevRow, airbornePrevCol)) {
            board.setPieceAt(airbornePrevRow, airbornePrevCol, airbornePiece);
            startRestAt(airbornePrevRow, airbornePrevCol, 600L);
        }
    }

    private void clearAirborne() {
        airbornePiece = null;
        airborneRow = -1;
        airborneCol = -1;
        airbornePrevRow = -1;
        airbornePrevCol = -1;
        airborneRemainingMs = 0L;
        airborneTotalMs = 0L;
        lastArrivedSrcRow = -1;
        lastArrivedSrcCol = -1;
        lastArrivedDestRow = -1;
        lastArrivedDestCol = -1;
    }

    /** Applies board updates for one completed motion. */
    private void resolveArrival(Motion motion) {
        lastArrivedSrcRow = motion.getSrcRow();
        lastArrivedSrcCol = motion.getSrcCol();
        lastArrivedDestRow = motion.getDestRow();
        lastArrivedDestCol = motion.getDestCol();

        Piece piece = motion.getPiece();
        Piece movingPiece = promotionRule != null
                ? promotionRule.checkPromotion(piece, motion.getDestRow(), board)
                : piece;
        int destRow = motion.getDestRow();
        int destCol = motion.getDestCol();

        Piece sourcePiece = board.getPieceAt(motion.getSrcRow(), motion.getSrcCol());

        if (sourcePiece == piece) {
            board.setPieceAt(motion.getSrcRow(), motion.getSrcCol(), null);
        }

        Piece target = board.getPieceAt(destRow, destCol);
        boolean kingCaptured = false;
        if (target != null && target.getColor() != movingPiece.getColor()) {
            if (target.getType() == PieceType.KING) {
                kingCaptured = true;
            }
        }

        board.setPieceAt(destRow, destCol, movingPiece);
        startRestAt(destRow, destCol, 1200L);

        if (kingCaptured) {
            winCondition.recordKingCapture(movingPiece.getColor());
        }
    }

    /** @return true if there is at least one active motion. */
    public boolean hasActiveMotion() {
        return !activeMotions.isEmpty();
    }

    /** Returns a copy of active motions. */
    public List<Motion> getActiveMotions() {
        return new ArrayList<>(activeMotions);
    }

    /** Returns true when the piece on this square is still in rest cooldown. */
    public boolean isPieceResting(int row, int col) {
        String key = squareKey(row, col);
        Long until = restUntilBySquare.get(key);
        if (until == null) {
            return false;
        }
        if (simulatedTimeMs >= until) {
            restUntilBySquare.remove(key);
            return false;
        }
        return board.getPieceAt(row, col) != null;
    }

    /** Returns true if an active motion currently targets this board cell. */
    public boolean isIncomingDestination(int row, int col) {
        for (Motion motion : activeMotions) {
            if (motion.getDestRow() == row && motion.getDestCol() == col) {
                return true;
            }
        }
        return false;
    }

    /** Returns current airborne jump state, or null if no jump is active. */
    public AirborneJump getAirborneJump() {
        if (airbornePiece == null) {
            return null;
        }
        return new AirborneJump(airbornePiece, airborneRow, airborneCol, airborneRemainingMs, airborneTotalMs > 0 ? airborneTotalMs : 1000L);
    }

    private void startRestAt(int row, int col, long restMs) {
        if (!board.isValid(row, col) || board.getPieceAt(row, col) == null) {
            return;
        }
        restUntilBySquare.put(squareKey(row, col), simulatedTimeMs + Math.max(1L, restMs));
    }

    private String squareKey(int row, int col) {
        return row + ":" + col;
    }
}
