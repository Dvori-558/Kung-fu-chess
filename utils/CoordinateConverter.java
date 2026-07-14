package utils;

/** Converts between pixel coordinates and board cells. */
public class CoordinateConverter {
    private final int pixelsPerCell;
    
    public CoordinateConverter(int pixelsPerCell) {
        if (pixelsPerCell <= 0) {
            throw new IllegalArgumentException("Pixels per cell must be positive");
        }
        this.pixelsPerCell = pixelsPerCell;
    }
    
    /** Converts pixel X to board column. */
    public int pixelToGridCol(int pixelX) {
        return Math.floorDiv(pixelX, pixelsPerCell);
    }
    
    /** Converts pixel Y to board row. */
    public int pixelToGridRow(int pixelY) {
        return Math.floorDiv(pixelY, pixelsPerCell);
    }
    
    /** Converts board column to pixel X (cell center). */
    public int gridColToPixelX(int col) {
        return col * pixelsPerCell + pixelsPerCell / 2;
    }
    
    /** Converts board row to pixel Y (cell center). */
    public int gridRowToPixelY(int row) {
        return row * pixelsPerCell + pixelsPerCell / 2;
    }

    public int getPixelsPerCell() {
        return pixelsPerCell;
    }
}
