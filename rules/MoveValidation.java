package rules;

/**
 * Result of move validation by RuleEngine.
 * Immutable value object carrying the validation result and a stable reason.
 */
public class MoveValidation {
    // Reason constants
    public static final String OUTSIDE_BOARD = "outside_board";
    public static final String EMPTY_SOURCE = "empty_source";
    public static final String FRIENDLY_DESTINATION = "friendly_destination";
    public static final String ILLEGAL_PIECE_MOVE = "illegal_piece_move";
    
    private final boolean isValid;
    private final String reason;
    
    /**
     * Private constructor. Use factory methods instead.
     */
    private MoveValidation(boolean isValid, String reason) {
        this.isValid = isValid;
        this.reason = reason != null ? reason : "unknown";
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public String getReason() {
        return reason;
    }
    
    @Override
    public String toString() {
        return "MoveValidation(isValid=" + isValid + ", reason=" + reason + ")";
    }
    
    // Factory methods for all valid cases
    public static MoveValidation ok() {
        return new MoveValidation(true, "ok");
    }
    
    // Factory methods for invalid cases
    public static MoveValidation invalid(String reason) {
        return new MoveValidation(false, reason);
    }
}
