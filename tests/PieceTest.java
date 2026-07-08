package tests;

import models.*;

/**
 * Unit tests for Piece class.
 * Tests the immutability and color checking logic.
 */
public class PieceTest {
    
    public static void main(String[] args) {
        int passed = 0;
        int total = 0;
        
        // Test 1: Create piece
        total++;
        try {
            Piece p = new Piece(Piece.WHITE, PieceType.PAWN);
            assert p.getColor() == Piece.WHITE;
            assert p.getType().equals(PieceType.PAWN);
            System.out.println("[PASS] Test 1: Create piece");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
        }
        
        // Test 2: Invalid color
        total++;
        try {
            new Piece('x', PieceType.PAWN);
            System.out.println("[FAIL] Test 2: Should reject invalid color");
        } catch (IllegalArgumentException e) {
            System.out.println("[PASS] Test 2: Rejects invalid color");
            passed++;
        }
        
        // Test 3: Same color check
        total++;
        try {
            Piece p1 = new Piece(Piece.WHITE, PieceType.PAWN);
            Piece p2 = new Piece(Piece.WHITE, PieceType.KNIGHT);
            assert p1.isSameColor(p2);
            assert !p1.isSameColor(null);
            System.out.println("[PASS] Test 3: Same color check");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 3: " + e.getMessage());
        }
        
        // Test 4: Opposite color check
        total++;
        try {
            Piece white = new Piece(Piece.WHITE, PieceType.PAWN);
            Piece black = new Piece(Piece.BLACK, PieceType.PAWN);
            assert white.isOppositeColor(black);
            assert black.isOppositeColor(white);
            assert !white.isOppositeColor(white);
            System.out.println("[PASS] Test 4: Opposite color check");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 4: " + e.getMessage());
        }
        
        // Test 5: toString
        total++;
        try {
            assert new Piece(Piece.WHITE, PieceType.PAWN).toString().equals("wP");
            assert new Piece(Piece.BLACK, PieceType.KNIGHT).toString().equals("bN");
            assert new Piece(Piece.WHITE, PieceType.KING).toString().equals("wK");
            System.out.println("[PASS] Test 5: toString");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 5: " + e.getMessage());
        }
        
        // Test 6: Binary encoding/decoding
        total++;
        try {
            Piece original = new Piece(Piece.BLACK, PieceType.QUEEN);
            byte encoded = original.toByte();
            
            // Create a type map
            PieceType[] typeMap = new PieceType[128];
            typeMap[PieceType.PAWN.getId()] = PieceType.PAWN;
            typeMap[PieceType.KNIGHT.getId()] = PieceType.KNIGHT;
            typeMap[PieceType.BISHOP.getId()] = PieceType.BISHOP;
            typeMap[PieceType.ROOK.getId()] = PieceType.ROOK;
            typeMap[PieceType.QUEEN.getId()] = PieceType.QUEEN;
            typeMap[PieceType.KING.getId()] = PieceType.KING;
            
            Piece decoded = Piece.fromByte(encoded, typeMap);
            assert decoded.equals(original);
            System.out.println("[PASS] Test 6: Binary encoding/decoding");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 6: " + e.getMessage());
        }
        
        System.out.println("\n=== Piece Tests: " + passed + "/" + total + " passed ===");
    }
}
