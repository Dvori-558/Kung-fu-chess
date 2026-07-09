package board;

import java.util.Scanner;
import engine.GameEngine;
import engine.GameEngineImpl;
import engine.GameSnapshot;
import io.BoardParser;
import io.BoardPrinter;
import rules.GameConfig;
import utils.CoordinateConverter;

/**
 * Main - application entry point.
 * Wires up all layers and runs the command loop.
 * 
 * Command flow per the design guide:
 * - Board text -> BoardParser -> Board
 * - click x y -> Controller.click(x, y)
 * - wait ms   -> GameEngine.pause(ms)
 * - print board -> BoardPrinter.print(snapshot)
 */
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        GameConfig config = new GameConfig.Builder().buildStandardChess().build();

        Board board = BoardParser.parse(sc, config);
        if (board.isEmpty()) {
            sc.close();
            return;
        }

        GameEngine engine = new GameEngineImpl(board, config);
        CoordinateConverter converter = new CoordinateConverter(config.getPixelsPerCell());
        Controller controller = new Controller(engine, board, converter);
        BoardPrinter printer = new BoardPrinter();

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("click")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    controller.click(x, y);
                }
            } else if (line.startsWith("wait") || line.startsWith("pause")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    long ms = Long.parseLong(parts[1]);
                    engine.pause(ms);
                }
            } else if (line.equals("print board")) {
                GameSnapshot snapshot = engine.snapshot();
                printer.print(snapshot.getBoard());
            }
        }

        sc.close();
    }
}
