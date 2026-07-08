package board;

import java.util.Scanner;
import engine.*;
import rules.GameConfig;

/**
 * Main class - refactored to be clean and extensible.
 */
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Create standard chess game configuration
        GameConfig config = new GameConfig.Builder().buildStandardChess().build();

        // Read board from input
        Board board = Board.readFrom(sc, config);

        if (board.isEmpty()) {
            sc.close();
            return;
        }

        if (!board.isValid()) {
            sc.close();
            return;
        }

        // Create engine, controller, adapter
        engine.GameEngine engine = new engine.GameEngineImpl(board, config);
        Controller controller = new Controller(engine);
        InputAdapter adapter = new InputAdapter();

        // Process commands
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            Command cmd = adapter.parse(line);
            controller.dispatch(cmd);
        }

        sc.close();
    }
}
