package models;

/**
 * Piece represents a single chess piece on the board.
 * 
 * Design Pattern: Value Object (immutable)
 * Purpose: Encapsulate piece state (color + type)
 * 
 * Benefits:
 * - Immutable = thread-safe, easy to reason about
 * - Can be encoded as 1 byte: [color (1 bit) | type_id (7 bits)]
 * - Supports custom piece types via PieceType
 */
public class Piece {
    public static final char WHITE = 'w';
    public static final char BLACK = 'b';
    
    private final char color;
    private final PieceType type;
    
    public Piece(char color, PieceType type) {
        if (color != WHITE && color != BLACK) {
            throw new IllegalArgumentException("Color must be 'w' or 'b'");
        }
        if (type == null) {
            throw new IllegalArgumentException("PieceType cannot be null");
        }
        this.color = color;
        this.type = type;
    }
    
    public char getColor() {
        return color;
    }
    
    public PieceType getType() {
        return type;
    }
    
    public boolean isSameColor(Piece other) {
        return other != null && this.color == other.color;
    }
    
    public boolean isOppositeColor(Piece other) {
        return other != null && this.color != other.color;
    }
    
    /**
     * Encode as byte for binary storage.
     * Format: [color (1 bit) | type_id (7 bits)]
     */
    public byte toByte() {
        byte colorBit = (byte) (color == WHITE ? 0 : 1);
        return (byte) ((colorBit << 7) | (type.getId() & 0x7F));
    }
    
    /**
     * Decode from byte (inverse of toByte).
     */
    public static Piece fromByte(byte b, PieceType[] typeMap) {
        char color = ((b & 0x80) != 0) ? BLACK : WHITE;
        byte typeId = (byte) (b & 0x7F);
        if (typeId >= typeMap.length || typeMap[typeId] == null) {
            throw new IllegalArgumentException("Unknown piece type ID: " + typeId);
        }
        return new Piece(color, typeMap[typeId]);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return color == piece.color && type.equals(piece.type);
    }
    
    @Override
    public int hashCode() {
        return 31 * color + type.hashCode();
    }
    
    @Override
    public String toString() {
        // Map piece types to their single-character representation
        char typeChar;
        if (type.equals(PieceType.PAWN)) typeChar = 'P';
        else if (type.equals(PieceType.KNIGHT)) typeChar = 'N';
        else if (type.equals(PieceType.BISHOP)) typeChar = 'B';
        else if (type.equals(PieceType.ROOK)) typeChar = 'R';
        else if (type.equals(PieceType.QUEEN)) typeChar = 'Q';
        else if (type.equals(PieceType.KING)) typeChar = 'K';
        else typeChar = '?';
        return "" + color + typeChar;
    }
    
    // ========== Standard Chess Pieces ==========
    public static Piece WHITE_PAWN = new Piece(WHITE, PieceType.PAWN);
    public static Piece WHITE_KNIGHT = new Piece(WHITE, PieceType.KNIGHT);
    public static Piece WHITE_BISHOP = new Piece(WHITE, PieceType.BISHOP);
    public static Piece WHITE_ROOK = new Piece(WHITE, PieceType.ROOK);
    public static Piece WHITE_QUEEN = new Piece(WHITE, PieceType.QUEEN);
    public static Piece WHITE_KING = new Piece(WHITE, PieceType.KING);
    
    public static Piece BLACK_PAWN = new Piece(BLACK, PieceType.PAWN);
    public static Piece BLACK_KNIGHT = new Piece(BLACK, PieceType.KNIGHT);
    public static Piece BLACK_BISHOP = new Piece(BLACK, PieceType.BISHOP);
    public static Piece BLACK_ROOK = new Piece(BLACK, PieceType.ROOK);
    public static Piece BLACK_QUEEN = new Piece(BLACK, PieceType.QUEEN);
    public static Piece BLACK_KING = new Piece(BLACK, PieceType.KING);
}
