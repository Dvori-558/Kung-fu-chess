package engine;

public class JumpCommand implements Command {
    public final int x;
    public final int y;

    public JumpCommand(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
