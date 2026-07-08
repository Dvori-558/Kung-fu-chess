package engine;

public class WaitCommand implements Command {
    public final int ms;
    public WaitCommand(int ms) { this.ms = ms; }
}
