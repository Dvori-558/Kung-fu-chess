package board;

import java.util.Scanner;
import engine.*;
import io.BoardParser;
import io.BoardPrinter;
import rules.GameConfig;
import utils.CoordinateConverter;

/**
 * Main class - refactored to use new clean API.
 * 
 * Workflow:
 * 1. Parse board with BoardParser
 * 2. Create GameEngine
 * 3. Create Controller with board and converter
 * 4. Process commands: click, pause, snapshot
 */
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Create standard chess game configuration
        GameConfig config = new GameConfig.Builder().buildStandardChess().build();

        // Parse board from textual input
        Board board = BoardParser.parse(sc, config);

        if (board.isEmpty()) {
            sc.close();
            return;
        }

        // Create engine
        GameEngine engine = new GameEngineImpl(board, config);

        // Create controller with board and converter for pixel->grid mapping
        CoordinateConverter converter = new CoordinateConverter(config.getPixelsPerCell());
        Controller controller = new Controller(engine, board, converter);

        // Create printer for output
        BoardPrinter printer = new BoardPrinter();

        // Main game loop
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            // Parse command
            if (line.startsWith("click")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    controller.click(x, y);
                }
            } else if (line.startsWith("pause")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    long ms = Long.parseLong(parts[1]);
                    engine.pause(ms);
                }
            } else if (line.startsWith("snapshot") || line.startsWith("print")) {
                GameSnapshot snapshot = engine.snapshot();
                printer.print(snapshot.getBoard());
            }
        }

        sc.close();
    }
}
