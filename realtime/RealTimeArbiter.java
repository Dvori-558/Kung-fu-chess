package realtime;

import java.util.ArrayList;
import java.util.List;

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
    private long simulatedTimeMs = 0;

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

    /** Advances time and resolves completed motions. */
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

    /** Applies board updates for one completed motion. */
    private void resolveArrival(Motion motion) {
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
}
