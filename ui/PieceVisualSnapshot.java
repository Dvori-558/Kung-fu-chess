package ui;

import models.Piece;

/** Immutable visual data for drawing one piece. */
public class PieceVisualSnapshot {
    private final Piece piece;
    private final double pixelX;
    private final double pixelY;
    private final String state;
    private final double progressHint;
    private final int row;
    private final int col;

    public PieceVisualSnapshot(Piece piece, double pixelX, double pixelY, String state, double progressHint, int row, int col) {
        this.piece = piece;
        this.pixelX = pixelX;
        this.pixelY = pixelY;
        this.state = state;
        this.progressHint = progressHint;
        this.row = row;
        this.col = col;
    }

    public Piece getPiece() { return piece; }
    public double getPixelX() { return pixelX; }
    public double getPixelY() { return pixelY; }
    public String getState() { return state; }
    public double getProgressHint() { return progressHint; }
    public int getRow() { return row; }
    public int getCol() { return col; }
}