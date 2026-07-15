package tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Regression test for reaction window on incoming attacks.
 */
public class IncomingAttackReactionTest {
    public static void main(String[] args) {
        int passed = 0;
        int total = 0;

        // Defender on attacked destination can jump away before arrival.
        total++;
        try {
            String input = "Board:\n" +
                    "wR . .\n" +
                    ". bP .\n" +
                    ". . .\n" +
                    "Commands:\n" +
                    "click 50 50\n" +       // select wR at (0,0)
                    "click 150 150\n" +     // rook moves toward (1,1)
                    "wait 500\n" +          // rook is in motion, not yet arrived
                    "jump 150 150\n" +      // defender bP jumps from attacked destination
                    "wait 1000\n" +         // jump resolves first
                    "wait 1000\n" +         // rook arrival resolves after
                    "print board\n";

            String actual = runScript(input);
            String expected = ". . .\n" +
                    ". wR .\n" +
                    ". bP .";

            assert expected.equals(actual) : "Expected:\n" + expected + "\nActual:\n" + actual;
            System.out.println("[PASS] Test 1: defender can react before incoming arrival");
            passed++;
        } catch (Exception e) {
            System.out.println("[FAIL] Test 1: " + e.getMessage());
        }

        System.out.println("\n=== Incoming Attack Reaction Tests: " + passed + "/" + total + " passed ===");
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