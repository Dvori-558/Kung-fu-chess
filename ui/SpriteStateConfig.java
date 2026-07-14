package ui;

/** Parsed values from one state-level config.json file. */
public class SpriteStateConfig {
    private final double speedCellsPerSec;
    private final int framesPerSec;
    private final boolean loop;
    private final String nextState;

    public SpriteStateConfig(double speedCellsPerSec, int framesPerSec, boolean loop, String nextState) {
        this.speedCellsPerSec = speedCellsPerSec;
        this.framesPerSec = framesPerSec;
        this.loop = loop;
        this.nextState = nextState;
    }

    public double getSpeedCellsPerSec() { return speedCellsPerSec; }
    public int getFramesPerSec() { return framesPerSec; }
    public boolean isLoop() { return loop; }
    public String getNextState() { return nextState; }

    public static SpriteStateConfig defaults() {
        return new SpriteStateConfig(0.0, 8, true, "idle");
    }
}