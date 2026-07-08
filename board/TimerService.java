package board;

public class TimerService {
    private final MoveManager moveManager;
    private final AirborneManager airborneManager;

    public TimerService(MoveManager moveManager, AirborneManager airborneManager) {
        this.moveManager = moveManager;
        this.airborneManager = airborneManager;
    }

    public void tick(int ms) {
        moveManager.tick(ms);
        airborneManager.tick(ms);
    }
}