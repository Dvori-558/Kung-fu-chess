package realtime;

import models.Piece;

/** Read-only snapshot of one airborne jump for UI rendering. */
public class AirborneJump {
    private final Piece piece;
    private final int row;
    private final int col;
    private final long remainingMs;
    private final long totalMs;

    public AirborneJump(Piece piece, int row, int col, long remainingMs, long totalMs) {
        this.piece = piece;
        this.row = row;
        this.col = col;
        this.remainingMs = remainingMs;
        this.totalMs = totalMs;
    }

    public Piece getPiece() { return piece; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public long getRemainingMs() { return remainingMs; }
    public long getTotalMs() { return totalMs; }

    public double getProgress() {
        if (totalMs <= 0) return 1.0;
        double p = 1.0 - ((double) remainingMs / (double) totalMs);
        if (p < 0.0) return 0.0;
        if (p > 1.0) return 1.0;
        return p;
    }
}