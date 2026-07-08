package board;

import engine.Command;

public class Controller {
    private final engine.GameEngine engine;

    public Controller(engine.GameEngine engine) {
        this.engine = engine;
    }

    public void dispatch(Command cmd) {
        if (cmd == null) return;
        engine.handle(cmd);
    }
}
