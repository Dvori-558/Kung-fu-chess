package engine;

import board.*;
import models.Piece;
import models.PieceType;
import rules.*;
import utils.CoordinateConverter;

public class GameEngineImpl implements GameEngine {
    private final Board board;
    private final GameConfig config;
    private final RuleEngine ruleEngine;
    private final MoveManager moveManager;
    private final AirborneManager airborneManager;
    private final TimerService timerService;
    private final PromotionService promotionService;
    private final WinManager winManager;
    private final CoordinateConverter converter;

    private int selectedRow = -1;
    private int selectedCol = -1;

    public GameEngineImpl(Board board, GameConfig config) {
        this.board = board;
        this.config = config;
        this.ruleEngine = new StandardRuleEngine(config);
        this.winManager = new WinManager(config.getWinCondition());
        this.promotionService = new PromotionService(config.getPromotionRule());
        this.airborneManager = new AirborneManager(board, config);
        this.moveManager = new MoveManager(board, config, promotionService, winManager, airborneManager);
        this.timerService = new TimerService(moveManager, airborneManager);
        this.converter = new CoordinateConverter(config.getPixelsPerCell());
    }

    @Override
    public void handle(Command cmd) {
        if (cmd instanceof ClickCommand) {
            ClickCommand c = (ClickCommand) cmd;
            handleClick(c.x, c.y);
        } else if (cmd instanceof JumpCommand) {
            JumpCommand j = (JumpCommand) cmd;
            handleJump(j.x, j.y);
        } else if (cmd instanceof WaitCommand) {
            WaitCommand w = (WaitCommand) cmd;
            handleWait(w.ms);
        } else if (cmd instanceof PrintBoardCommand) {
            board.printBoard();
        }
    }

    private void handleClick(int x, int y) {
        if (winManager.isGameOver() || moveManager.isMovePending()) return;
        int col = converter.pixelToGridCol(x);
        int row = converter.pixelToGridRow(y);
        if (!board.isValid(row, col)) return;

        Piece target = board.getPieceAt(row, col);

        if (selectedRow == -1 || selectedCol == -1) {
            if (target != null) {
                selectedRow = row;
                selectedCol = col;
            }
            return;
        }

        Piece selectedPiece = board.getPieceAt(selectedRow, selectedCol);
        if (selectedRow == row && selectedCol == col) return;

        if (target == null) {
            if (ruleEngine.isValidMove(selectedPiece, selectedRow, selectedCol, row, col, board)) {
                moveManager.startMove(selectedPiece, selectedRow, selectedCol, row, col);
                selectedRow = -1; selectedCol = -1;
            }
            return;
        }

        if (selectedPiece.isSameColor(target)) {
            selectedRow = row; selectedCol = col; return;
        }

        if (ruleEngine.isValidMove(selectedPiece, selectedRow, selectedCol, row, col, board)) {
            moveManager.startMove(selectedPiece, selectedRow, selectedCol, row, col);
            selectedRow = -1; selectedCol = -1;
        } else {
            selectedRow = row; selectedCol = col;
        }
    }

    private void handleJump(int x, int y) {
        if (winManager.isGameOver() || airborneManager.hasAirborne() || moveManager.isMovePending()) return;
        int col = converter.pixelToGridCol(x);
        int row = converter.pixelToGridRow(y);
        if (!board.isValid(row, col)) return;
        Piece piece = board.getPieceAt(row, col);
        if (piece != null) airborneManager.startJump(piece, row, col);
    }

    private void handleWait(int ms) {
        if (winManager.isGameOver()) return;
        timerService.tick(ms);
    }
}
