package board;

import engine.GameEngine;
import engine.MoveResult;
import models.Piece;
import utils.CoordinateConverter;

/** Handles click selection and forwards move requests to the engine. */
public class Controller {
    private final GameEngine engine;
    private final Board board;
    private final CoordinateConverter converter;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private String lastMoveReason = null;

    public Controller(GameEngine engine, Board board, CoordinateConverter converter) {
        this.engine = engine;
        this.board = board;
        this.converter = converter;
    }

    /** Processes one click in pixel coordinates. */
    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public String getLastMoveReason() {
        return lastMoveReason;
    }

    public void click(int pixelX, int pixelY) {
        lastMoveReason = null;
        int col = converter.pixelToGridCol(pixelX);
        int row = converter.pixelToGridRow(pixelY);

        if (!board.isValid(row, col)) {
            return;
        }

        Piece target = board.getPieceAt(row, col);

        if (selectedRow == -1 || selectedCol == -1) {
            if (target != null) {
                selectedRow = row;
                selectedCol = col;
            }
            return;
        }

        if (selectedRow == row && selectedCol == col) {
            selectedRow = -1;
            selectedCol = -1;
            return;
        }

        Piece selectedPiece = board.getPieceAt(selectedRow, selectedCol);
        if (selectedPiece != null && target != null && target.getColor() == selectedPiece.getColor()) {
            selectedRow = row;
            selectedCol = col;
            return;
        }

        MoveResult result = engine.requestMove(selectedRow, selectedCol, row, col);
        if (!result.isAccepted()) {
            lastMoveReason = result.getReason();
        }
        selectedRow = -1;
        selectedCol = -1;
    }

    /** Converts a jump click to board coordinates and forwards it to the engine. */
    public void jump(int pixelX, int pixelY) {
        lastMoveReason = null;

        int row;
        int col;

        // Prefer jumping the currently selected piece for better UI ergonomics.
        if (selectedRow >= 0 && selectedCol >= 0 && board.getPieceAt(selectedRow, selectedCol) != null) {
            row = selectedRow;
            col = selectedCol;
        } else {
            col = converter.pixelToGridCol(pixelX);
            row = converter.pixelToGridRow(pixelY);
        }

        if (!board.isValid(row, col)) {
            lastMoveReason = "Jump rejected: outside board";
            selectedRow = -1;
            selectedCol = -1;
            return;
        }

        if (!engine.requestJump(row, col)) {
            lastMoveReason = "Jump rejected";
        }

        selectedRow = -1;
        selectedCol = -1;
    }
}
