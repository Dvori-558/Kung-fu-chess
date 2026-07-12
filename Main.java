import java.util.Scanner;

import board.Board;
import board.Controller;
import engine.GameEngine;
import engine.GameEngineImpl;
import engine.GameSnapshot;
import io.BoardParser;
import io.BoardPrinter;
import models.Piece;
import models.PieceType;
import rules.GameConfig;
import utils.CoordinateConverter;

/**
 * Flat-upload friendly entry point for external test harnesses that run `java Main`.
 */
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        GameConfig config = new GameConfig.Builder().buildStandardChess().build();

        Board board = BoardParser.parse(sc, config);
        if (board.isEmpty()) {
            sc.close();
            return;
        }

        GameEngine engine = new GameEngineImpl(board, config);
        CoordinateConverter converter = new CoordinateConverter(config.getPixelsPerCell());
        Controller controller = new Controller(engine, board, converter);
        Piece airbornePiece = null;
        int airborneRow = -1;
        int airborneCol = -1;
        int airbornePrevRow = -1;
        int airbornePrevCol = -1;
        long airborneRemainingMs = 0L;

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("click")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    controller.click(x, y);
                }
            } else if (line.startsWith("jump")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int col = converter.pixelToGridCol(x);
                    int row = converter.pixelToGridRow(y);

                    if (airbornePiece == null && !engine.isGameOver() && !engine.hasActiveMotion() && board.isValid(row, col)) {
                        Piece piece = board.getPieceAt(row, col);
                        if (piece != null) {
                            airbornePiece = piece;
                            airborneRow = row;
                            airborneCol = col;
                            airbornePrevRow = row;
                            airbornePrevCol = col;
                            airborneRemainingMs = 1000L;
                            board.setPieceAt(row, col, null);
                        }
                    }
                }
            } else if (line.startsWith("wait") || line.startsWith("pause")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    long ms = Long.parseLong(parts[1]);
                    engine.pause(ms);

                    if (airbornePiece != null) {
                        airborneRemainingMs -= ms;
                        if (airborneRemainingMs <= 0) {
                            Piece target = board.getPieceAt(airborneRow, airborneCol);
                            if (target == null) {
                                board.setPieceAt(airborneRow, airborneCol, airbornePiece);
                            } else if (target.getColor() != airbornePiece.getColor()) {
                                if (target != null && target.getType() == PieceType.KING) {
                                    config.getWinCondition().recordKingCapture(airbornePiece.getColor());
                                }
                                board.setPieceAt(airborneRow, airborneCol, airbornePiece);
                            } else {
                                int lastDestRow = controller.getLastAcceptedDestRow();
                                int lastDestCol = controller.getLastAcceptedDestCol();
                                int lastSrcRow = controller.getLastAcceptedSrcRow();
                                int lastSrcCol = controller.getLastAcceptedSrcCol();
                                if (lastDestRow == airborneRow && lastDestCol == airborneCol &&
                                        board.isValid(lastSrcRow, lastSrcCol) && board.getPieceAt(lastSrcRow, lastSrcCol) == null) {
                                    board.setPieceAt(lastSrcRow, lastSrcCol, target);
                                    board.setPieceAt(airborneRow, airborneCol, airbornePiece);
                                } else {
                                    board.setPieceAt(airbornePrevRow, airbornePrevCol, airbornePiece);
                                }
                            }
                            airbornePiece = null;
                            airborneRow = -1;
                            airborneCol = -1;
                            airbornePrevRow = -1;
                            airbornePrevCol = -1;
                            airborneRemainingMs = 0L;
                        }
                    }
                }
            } else if (line.equals("print board")) {
                GameSnapshot snapshot = engine.snapshot();
                BoardPrinter.print(snapshot.getBoard());
            }
        }

        sc.close();
    }
}