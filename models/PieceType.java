package models;

/** Immutable descriptor for piece identity and id. */
public class PieceType {
    private final String name;
    private final byte id;  // 0-127, for 1-byte encoding (color uses 1 bit)
    
    public PieceType(String name, byte id) {
        if (id < 0 || id > 127) {
            throw new IllegalArgumentException("Piece type ID must be 0-127");
        }
        this.name = name;
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public byte getId() {
        return id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PieceType that = (PieceType) o;
        return id == that.id && name.equals(that.name);
    }
    
    @Override
    public int hashCode() {
        return 31 * name.hashCode() + id;
    }
    
    @Override
    public String toString() {
        return name + "(" + id + ")";
    }
    
    // Standard chess piece types
    public static final PieceType PAWN = new PieceType("Pawn", (byte) 1);
    public static final PieceType KNIGHT = new PieceType("Knight", (byte) 2);
    public static final PieceType BISHOP = new PieceType("Bishop", (byte) 3);
    public static final PieceType ROOK = new PieceType("Rook", (byte) 4);
    public static final PieceType QUEEN = new PieceType("Queen", (byte) 5);
    public static final PieceType KING = new PieceType("King", (byte) 6);
}
