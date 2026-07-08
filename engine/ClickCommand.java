package engine;

public class ClickCommand implements Command {
    public final int x;
    public final int y;

    public ClickCommand(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
