package tests;

import board.Board;
import board.Controller;
import engine.GameEngine;
import engine.GameEngineImpl;
import io.BoardParser;
import models.Piece;
import models.PieceType;
import rules.GameConfig;
import utils.CoordinateConverter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Contract tests for harness-facing behavior.
 * Focuses on parser error strings and controller click semantics.
 */
public class IntegrationContractTest {
    public static void main(String[] args) {
        int passed = 0;
        int total = 0;

        // Test 1: Unknown token prints expected error
        total++;
        try {
            GameConfig config = new GameConfig.Builder().buildStandardChess().build();
            String input = "Board:\n" +
                           "wK xZ\n" +
                           ". .\n" +
                           "Commands:\n";

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            Board board;
            try {
                board = BoardParser.parse(new Scanner(input), config);
            } finally {
                System.setOut(originalOut);
            }

            String printed = out.toString(StandardCharsets.UTF_8).trim();
            assert printed.equals("ERROR UNKNOWN_TOKEN") : "Expected ERROR UNKNOWN_TOKEN, got: " + printed;
            assert board.isEmpty() : "Board should be empty on parse error";
            System.out.println("[PASS] Test 1: unknown token error contract");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
        }

        // Test 2: Row width mismatch prints expected error
        total++;
        try {
            GameConfig config = new GameConfig.Builder().buildStandardChess().build();
            String input = "Board:\n" +
                           "wK . .\n" +
                           ". bK\n" +
                           "Commands:\n";

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            Board board;
            try {
                board = BoardParser.parse(new Scanner(input), config);
            } finally {
                System.setOut(originalOut);
            }

            String printed = out.toString(StandardCharsets.UTF_8).trim();
            assert printed.equals("ERROR ROW_WIDTH_MISMATCH") : "Expected ERROR ROW_WIDTH_MISMATCH, got: " + printed;
            assert board.isEmpty() : "Board should be empty on parse error";
            System.out.println("[PASS] Test 2: row width mismatch error contract");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 2: " + e.getMessage());
        }

        // Test 3: Negative click is ignored (outside board)
        total++;
        try {
            GameConfig config = new GameConfig.Builder().buildStandardChess().build();
            Piece[][] grid = new Piece[3][3];
            Piece king = new Piece(Piece.WHITE, PieceType.KING);
            grid[0][0] = king;
            Board board = Board.create(grid, config);
            GameEngine engine = new GameEngineImpl(board, config);
            Controller controller = new Controller(engine, board, new CoordinateConverter(100));

            controller.click(-10, 50);
            controller.click(150, 150);
            engine.pause(1000);

            assert board.getPieceAt(0, 0) == king : "King should remain at origin when first click is outside";
            assert board.getPieceAt(1, 1) == null : "Destination should remain empty";
            System.out.println("[PASS] Test 3: negative click ignored");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 3: " + e.getMessage());
        }

        // Test 4: Clicking another own piece replaces selection
        total++;
        try {
            GameConfig config = new GameConfig.Builder().buildStandardChess().build();
            Piece[][] grid = new Piece[2][3];
            Piece rook = new Piece(Piece.WHITE, PieceType.ROOK);
            Piece king = new Piece(Piece.WHITE, PieceType.KING);
            grid[0][0] = rook;
            grid[0][2] = king;
            Board board = Board.create(grid, config);
            GameEngine engine = new GameEngineImpl(board, config);
            Controller controller = new Controller(engine, board, new CoordinateConverter(100));

            controller.click(50, 50);
            controller.click(250, 50);
            controller.click(250, 150);
            engine.pause(1000);

            assert board.getPieceAt(0, 0) == rook : "Rook should remain at source";
            assert board.getPieceAt(0, 2) == null : "King should move from original square";
            assert board.getPieceAt(1, 2) == king : "King should move one step down";
            System.out.println("[PASS] Test 4: replace selection on own piece click");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 4: " + e.getMessage());
        }

        System.out.println("\n=== Integration Contract Tests: " + passed + "/" + total + " passed ===");
    }
}