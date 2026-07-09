package realtime;

import models.Piece;

/**
 * Motion tracks a single piece moving from source to destination over simulated time.
 * 
 * Owns:
 * - Source and destination coordinates
 * - Duration and elapsed time tracking
 * - Arrival detection
 * 
 * Does NOT own:
 * - Board mutation
 * - Chess rules
 * - Rendering
 */
public class Motion {
    private final Piece piece;
    private final int srcRow;
    private final int srcCol;
    private final int destRow;
    private final int destCol;
    private final long durationMs;
    private long elapsedMs = 0;

    public Motion(Piece piece, int srcRow, int srcCol, int destRow, int destCol, long durationMs) {
        this.piece = piece;
        this.srcRow = srcRow;
        this.srcCol = srcCol;
        this.destRow = destRow;
        this.destCol = destCol;
        this.durationMs = durationMs;
    }

    public void advance(long deltaMs) {
        elapsedMs += deltaMs;
    }

    public boolean hasArrived() {
        return elapsedMs >= durationMs;
    }

    public double getProgress() {
        if (durationMs <= 0) return 1.0;
        double p = (double) elapsedMs / durationMs;
        return Math.min(1.0, p);
    }

    public Piece getPiece()   { return piece; }
    public int getSrcRow()    { return srcRow; }
    public int getSrcCol()    { return srcCol; }
    public int getDestRow()   { return destRow; }
    public int getDestCol()   { return destCol; }
    public long getDurationMs() { return durationMs; }
    public long getElapsedMs()  { return elapsedMs; }
}
