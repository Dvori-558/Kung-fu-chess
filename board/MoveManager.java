package board;

import models.Piece;
import models.PieceType;
import rules.GameConfig;

public class MoveManager {
    private final Board board;
    private final GameConfig config;
    private final PromotionService promotionService;
    private final WinManager winManager;
    private final AirborneManager airborneManager;

    private boolean movePending = false;
    private Piece pendingPiece;
    private int fromRow, fromCol, toRow, toCol;
    private int remainingMs = 0;

    public MoveManager(Board board, GameConfig config, PromotionService promotionService, WinManager winManager, AirborneManager airborneManager) {
        this.board = board;
        this.config = config;
        this.promotionService = promotionService;
        this.winManager = winManager;
        this.airborneManager = airborneManager;
    }

    public boolean isMovePending() {
        return movePending;
    }

    public void startMove(Piece piece, int fr, int fc, int tr, int tc) {
        this.movePending = true;
        this.pendingPiece = piece;
        this.fromRow = fr;
        this.fromCol = fc;
        this.toRow = tr;
        this.toCol = tc;
        this.remainingMs = config.getMoveDurationMs();
    }

    public void tick(int ms) {
        if (!movePending) return;
        remainingMs -= ms;
        if (remainingMs <= 0) {
            completeMove();
        }
    }

    private void completeMove() {
        // Air capture
        if (airborneManager.hasAirborne() && airborneManager.getAirborneRow() == toRow && airborneManager.getAirborneCol() == toCol
                && airborneManager.getAirbornePiece().isOppositeColor(pendingPiece)) {
            board.setPieceAt(fromRow, fromCol, null);
            movePending = false;
            return;
        }

        Piece destination = board.getPieceAt(toRow, toCol);
        Piece finalPiece = promotionService.applyPromotion(pendingPiece, toRow, board);

        // King capture -> win
        if (destination != null && destination.getType().equals(PieceType.KING) && destination.isOppositeColor(pendingPiece)) {
            board.setPieceAt(fromRow, fromCol, null);
            board.setPieceAt(toRow, toCol, finalPiece);
            winManager.recordKingCapture(pendingPiece.getColor());
            movePending = false;
            return;
        }

        // Standard move
        board.setPieceAt(fromRow, fromCol, null);
        board.setPieceAt(toRow, toCol, finalPiece);
        movePending = false;
    }
}