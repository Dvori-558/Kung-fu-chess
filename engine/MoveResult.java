package engine;

/**
 * Result of a move request to GameEngine.
 * Immutable value object carrying acceptance status and reason string.
 * 
 * Reasons for accepted moves: "ok"
 * Reasons for rejected moves: "game_over", "motion_in_progress", 
 *                             or any reason from RuleEngine validation.
 */
public class MoveResult {
    // Reason constants
    public static final String OK = "ok";
    public static final String GAME_OVER = "game_over";
    public static final String MOTION_IN_PROGRESS = "motion_in_progress";
    
    private final boolean isAccepted;
    private final String reason;
    
    /**
     * Private constructor. Use factory methods instead.
     */
    private MoveResult(boolean isAccepted, String reason) {
        this.isAccepted = isAccepted;
        this.reason = reason != null ? reason : "unknown";
    }
    
    public boolean isAccepted() {
        return isAccepted;
    }
    
    public String getReason() {
        return reason;
    }
    
    @Override
    public String toString() {
        return "MoveResult(isAccepted=" + isAccepted + ", reason=" + reason + ")";
    }
    
    // Factory method for accepted move
    public static MoveResult accepted() {
        return new MoveResult(true, OK);
    }
    
    // Factory methods for application-level rejections
    public static MoveResult rejected(String reason) {
        return new MoveResult(false, reason);
    }
}
