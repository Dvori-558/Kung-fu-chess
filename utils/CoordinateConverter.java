package utils;

/**
 * CoordinateConverter handles pixel-to-grid coordinate conversion.
 * 
 * Design: Utility class (no magic numbers)
 * Purpose: Centralize coordinate conversion logic, support custom board sizes
 */
public class CoordinateConverter {
    private final int pixelsPerCell;
    
    public CoordinateConverter(int pixelsPerCell) {
        if (pixelsPerCell <= 0) {
            throw new IllegalArgumentException("Pixels per cell must be positive");
        }
        this.pixelsPerCell = pixelsPerCell;
    }
    
    /**
     * Convert pixel X coordinate to grid column.
     */
    public int pixelToGridCol(int pixelX) {
        return pixelX / pixelsPerCell;
    }
    
    /**
     * Convert pixel Y coordinate to grid row.
     */
    public int pixelToGridRow(int pixelY) {
        return pixelY / pixelsPerCell;
    }
    
    /**
     * Convert grid column to pixel X coordinate (center of cell).
     */
    public int gridColToPixelX(int col) {
        return col * pixelsPerCell + pixelsPerCell / 2;
    }
    
    /**
     * Convert grid row to pixel Y coordinate (center of cell).
     */
    public int gridRowToPixelY(int row) {
        return row * pixelsPerCell + pixelsPerCell / 2;
    }
}
