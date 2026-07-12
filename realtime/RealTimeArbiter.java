package realtime;

import java.util.ArrayList;
import java.util.List;

import board.Board;
import rules.PromotionRule;
import rules.WinCondition;
import models.Piece;
import models.PieceType;

/**
 * RealTimeArbiter manages all active motions and simulated time.
 * 
 * Owns:
 * - Collection of active Motion objects
 * - Simulated time counter
 * - Arrival resolution (atomic: remove source, capture, place at dest)
 * - King-capture reporting to WinManager
 * 
 * Does NOT own:
 * - Chess movement rules (RuleEngine)
 * - Board logical occupancy decisions (Board)
 * - Click handling (Controller)
 * - Rendering (Renderer)
 * 
 * Per the design guide:
 * - Board represents logical occupancy only
 * - RealTimeArbiter owns active motion state
 * - Logical board changes ONLY on arrival
 */
public class RealTimeArbiter {
    private final Board board;
    private final WinCondition winCondition;
    private final PromotionRule promotionRule;
    private final List<Motion> activeMotions = new ArrayList<>();
    private long simulatedTimeMs = 0;

    public RealTimeArbiter(Board board, WinCondition winCondition, PromotionRule promotionRule) {
        this.board = board;
        this.winCondition = winCondition;
        this.promotionRule = promotionRule;
    }

    /**
     * Start a new motion for a validated move.
     * Board is NOT changed here - only on arrival.
     */
    public void startMotion(Piece piece, int srcRow, int srcCol, int destRow, int destCol, long durationMs) {
        Motion motion = new Motion(piece, srcRow, srcCol, destRow, destCol, durationMs);
        activeMotions.add(motion);
    }

    /**
     * Advance simulated time by deltaMs.
     * Resolves any arrivals atomically.
     * This is called by GameEngine.pause(ms).
     */
    public void advanceTime(long deltaMs) {
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
    }

    /**
     * Atomic arrival resolution:
     * 1. Remove piece from source
     * 2. Capture enemy at destination (if any)
     * 3. Place piece at destination
     * 4. Report king capture to WinManager (if applicable)
     */
    private void resolveArrival(Motion motion) {
        Piece piece = motion.getPiece();
        Piece movingPiece = promotionRule != null
                ? promotionRule.checkPromotion(piece, motion.getDestRow(), board)
                : piece;
        int destRow = motion.getDestRow();
        int destCol = motion.getDestCol();

        Piece sourcePiece = board.getPieceAt(motion.getSrcRow(), motion.getSrcCol());

        // Step 1: Remove from source only if the same moving piece is still there
        if (sourcePiece == piece) {
            board.setPieceAt(motion.getSrcRow(), motion.getSrcCol(), null);
        }

        // Step 2: Capture enemy at destination if present
        Piece target = board.getPieceAt(destRow, destCol);
        boolean kingCaptured = false;
        if (target != null && target.getColor() != movingPiece.getColor()) {
            if (target.getType() == PieceType.KING) {
                kingCaptured = true;
            }
            // Enemy piece is removed (overwritten by step 3)
        }

        // Step 3: Place piece at destination
        board.setPieceAt(destRow, destCol, movingPiece);

        // Step 4: Report king capture
        if (kingCaptured) {
            winCondition.recordKingCapture(movingPiece.getColor());
        }
    }

    /**
     * Returns true if any motion is currently active.
     * Used by GameEngine for the motion_in_progress guard.
     */
    public boolean hasActiveMotion() {
        return !activeMotions.isEmpty();
    }

    /**
     * Returns a read-only view of active motions (for snapshot/rendering).
     */
    public List<Motion> getActiveMotions() {
        return new ArrayList<>(activeMotions);
    }
}
