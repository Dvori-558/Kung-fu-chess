package tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Extra edge-case tests for jump/airborne behavior.
 * These run through the public command path by executing board.Main with scripted input.
 */
public class JumpEdgeCasesTest {

    public static void main(String[] args) {
        int passed = 0;
        int total = 0;

        // Test 1: airborne piece captures arriving enemy
        total++;
        try {
            String input = "Board:\n" +
                    ". . .\n" +
                    "wK bR .\n" +
                    ". . .\n" +
                    "Commands:\n" +
                    "jump 50 150\n" +
                    "click 150 150\n" +
                    "click 50 150\n" +
                    "wait 1000\n" +
                    "print board\n";

            String expected = ". . .\n" +
                    "wK . .\n" +
                    ". . .";

            String actual = runScript(input);
            assert expected.equals(actual) : "Expected:\n" + expected + "\nActual:\n" + actual;
            System.out.println("[PASS] Test 1: airborne captures arriving enemy");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
        }

        // Test 2: jump too late does not save piece
        total++;
        try {
            String input = "Board:\n" +
                    ". . .\n" +
                    "wK bR .\n" +
                    ". . .\n" +
                    "Commands:\n" +
                    "click 150 150\n" +
                    "click 50 150\n" +
                    "wait 1000\n" +
                    "jump 50 150\n" +
                    "print board\n";

            String expected = ". . .\n" +
                    "bR . .\n" +
                    ". . .";

            String actual = runScript(input);
            assert expected.equals(actual) : "Expected:\n" + expected + "\nActual:\n" + actual;
            System.out.println("[PASS] Test 2: jump too late does not save");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 2: " + e.getMessage());
        }

        // Test 3: cannot jump while moving
        total++;
        try {
            String input = "Board:\n" +
                    "wR . .\n" +
                    "Commands:\n" +
                    "click 50 50\n" +
                    "click 250 50\n" +
                    "wait 500\n" +
                    "jump 50 50\n" +
                    "wait 1500\n" +
                    "print board\n";

            String expected = ". . wR";

            String actual = runScript(input);
            assert expected.equals(actual) : "Expected:\n" + expected + "\nActual:\n" + actual;
            System.out.println("[PASS] Test 3: cannot jump while moving");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 3: " + e.getMessage());
        }

        // Test 4: enemy arrives after landing captures normally
        total++;
        try {
            String input = "Board:\n" +
                    ". . . .\n" +
                    "wK . . bR\n" +
                    ". . . .\n" +
                    "Commands:\n" +
                    "jump 50 150\n" +
                    "wait 1000\n" +
                    "click 350 150\n" +
                    "click 50 150\n" +
                    "wait 3000\n" +
                    "print board\n";

            String expected = ". . . .\n" +
                    "bR . . .\n" +
                    ". . . .";

            String actual = runScript(input);
            assert expected.equals(actual) : "Expected:\n" + expected + "\nActual:\n" + actual;
            System.out.println("[PASS] Test 4: enemy arrives after landing captures");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 4: " + e.getMessage());
        }

        // Test 5: airborne capture only enemy
        total++;
        try {
            String input = "Board:\n" +
                    ". . .\n" +
                    "wK wR .\n" +
                    ". . .\n" +
                    "Commands:\n" +
                    "jump 50 150\n" +
                    "click 150 150\n" +
                    "click 50 150\n" +
                    "wait 1000\n" +
                    "print board\n";

            String expected = ". . .\n" +
                    "wK wR .\n" +
                    ". . .";

            String actual = runScript(input);
            assert expected.equals(actual) : "Expected:\n" + expected + "\nActual:\n" + actual;
            System.out.println("[PASS] Test 5: airborne capture only enemy");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 5: " + e.getMessage());
        }

        System.out.println("\n=== Jump Edge Cases Tests: " + passed + "/" + total + " passed ===");
    }

    private static String runScript(String input) {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        try {
            ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            System.setIn(in);
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));

            board.Main.main(new String[0]);
            return normalize(out.toString(StandardCharsets.UTF_8));
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    private static String normalize(String text) {
        return text.replace("\r\n", "\n").trim();
    }
}