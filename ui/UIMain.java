package ui;

import board.Board;
import board.Controller;
import engine.GameEngine;
import engine.GameEngineImpl;
import io.BoardParser;
import rules.GameConfig;
import utils.CoordinateConverter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Entry point for the Graphical User Interface.
 */
public class UIMain {
    public static void main(String[] args) {
        // BoardParser expects w/b prefix and optional Board:/Commands: markers.
        String initialBoard =
            "Board:\n" +
            "bR bN bB bQ bK bB bN bR\n" +
            "bP bP bP bP bP bP bP bP\n" +
            ". . . . . . . .\n" +
            ". . . . . . . .\n" +
            ". . . . . . . .\n" +
            ". . . . . . . .\n" +
            "wP wP wP wP wP wP wP wP\n" +
            "wR wN wB wQ wK wB wN wR\n" +
            "Commands:\n";

        GameConfig config = new GameConfig.Builder().buildStandardChess().build();
        InputStream is = new ByteArrayInputStream(initialBoard.getBytes());
        Scanner sc = new Scanner(is);
        
        Board board = BoardParser.parse(sc, config);
        GameEngine engine = new GameEngineImpl(board, config);
        CoordinateConverter converter = new CoordinateConverter(config.getPixelsPerCell());
        Controller controller = new Controller(engine, board, converter);

        GameUI.launch(engine, controller, config);
    }
}