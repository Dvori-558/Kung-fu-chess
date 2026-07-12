package texttests;

import board.Controller;
import engine.GameEngine;
import engine.GameSnapshot;
import io.BoardPrinter;

/**
 * ScriptRunner executes text integration DSL commands through the public command path.
 */
public class ScriptRunner {
    private final Controller controller;
    private final GameEngine engine;
    private final BoardPrinter printer;
    private final ScriptParser parser;

    public ScriptRunner(Controller controller, GameEngine engine, BoardPrinter printer) {
        this.controller = controller;
        this.engine = engine;
        this.printer = printer;
        this.parser = new ScriptParser();
    }

    /**
     * Execute a single script line.
     */
    public void runLine(String line) {
        ScriptParser.ParsedCommand command = parser.parse(line);

        switch (command.getType()) {
            case CLICK:
                controller.click(command.getX(), command.getY());
                break;
            case WAIT:
                engine.pause(command.getDurationMs());
                break;
            case PRINT_BOARD:
                GameSnapshot snapshot = engine.snapshot();
                printer.print(snapshot.getBoard());
                break;
            case EMPTY:
            case UNKNOWN:
            default:
                // Ignore empty or unknown lines for a stable text-runner command loop.
                break;
        }
    }
}