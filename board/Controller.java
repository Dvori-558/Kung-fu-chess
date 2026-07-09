package board;

import engine.GameEngine;
import models.Piece;
import utils.CoordinateConverter;

/**
 * Controller orchestrates user input and dispatches moves to GameEngine.
 * Owns selection state (which piece is selected).
 * 
 * Workflow:
 * 1. First click: select a piece
 * 2. Second click: send requestMove to GameEngine
 * GameEngine returns MoveResult with reason string.
 */
public class Controller {
    private final GameEngine engine;
    private final Board board;
    private final CoordinateConverter converter;
    private int selectedRow = -1;
    private int selectedCol = -1;

    public Controller(GameEngine engine, Board board, CoordinateConverter converter) {
        this.engine = engine;
        this.board = board;
        this.converter = converter;
    }

    /**
     * Handle a click at pixel coordinates.
     * Manages selection and sends move request to GameEngine.
     */
    public void click(int pixelX, int pixelY) {
        int col = converter.pixelToGridCol(pixelX);
        int row = converter.pixelToGridRow(pixelY);

        if (!board.isValid(row, col)) {
            return;
        }

        Piece target = board.getPieceAt(row, col);

        // First click: select a piece
        if (selectedRow == -1 || selectedCol == -1) {
            if (target != null) {
                selectedRow = row;
                selectedCol = col;
            }
            return;
        }

        // Deselect if same cell
        if (selectedRow == row && selectedCol == col) {
            selectedRow = -1;
            selectedCol = -1;
            return;
        }

        // Second click: send move request
        engine.requestMove(selectedRow, selectedCol, row, col);
        selectedRow = -1;
        selectedCol = -1;
    }
}
