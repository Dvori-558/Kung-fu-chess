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
    private int lastAcceptedSrcRow = -1;
    private int lastAcceptedSrcCol = -1;
    private int lastAcceptedDestRow = -1;
    private int lastAcceptedDestCol = -1;

    public Controller(GameEngine engine, Board board, CoordinateConverter converter) {
        this.engine = engine;
        this.board = board;
        this.converter = converter;
    }

    /** Processes one click in pixel coordinates. */
    public void click(int pixelX, int pixelY) {
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
        if (result.isAccepted()) {
            lastAcceptedSrcRow = selectedRow;
            lastAcceptedSrcCol = selectedCol;
            lastAcceptedDestRow = row;
            lastAcceptedDestCol = col;
        }
        selectedRow = -1;
        selectedCol = -1;
    }

    public int getLastAcceptedSrcRow() {
        return lastAcceptedSrcRow;
    }

    public int getLastAcceptedSrcCol() {
        return lastAcceptedSrcCol;
    }

    public int getLastAcceptedDestRow() {
        return lastAcceptedDestRow;
    }

    public int getLastAcceptedDestCol() {
        return lastAcceptedDestCol;
    }
}
