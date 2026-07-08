package board;

import models.Piece;
import rules.GameConfig;

public class AirborneManager {
    private final Board board;
    private final GameConfig config;

    private Piece airbornePiece = null;
    private int airborneRow = -1;
    private int airborneCol = -1;
    private int remainingMs = 0;

    public AirborneManager(Board board, GameConfig config) {
        this.board = board;
        this.config = config;
    }

    public boolean hasAirborne() {
        return airbornePiece != null;
    }

    public Piece getAirbornePiece() {
        return airbornePiece;
    }

    public int getAirborneRow() {
        return airborneRow;
    }

    public int getAirborneCol() {
        return airborneCol;
    }

    public void startJump(Piece piece, int row, int col) {
        this.airbornePiece = piece;
        this.airborneRow = row;
        this.airborneCol = col;
        this.remainingMs = config.getMoveDurationMs();
        board.setPieceAt(row, col, null);
    }

    public void tick(int ms) {
        if (airbornePiece == null) return;
        remainingMs -= ms;
        if (remainingMs <= 0) {
            board.setPieceAt(airborneRow, airborneCol, airbornePiece);
            airbornePiece = null;
            airborneRow = -1;
            airborneCol = -1;
            remainingMs = 0;
        }
    }
}