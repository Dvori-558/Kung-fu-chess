import java.io.*;
import java.util.*;
import io.BoardParser;
import rules.GameConfig;
public class TmpCheck {
  public static void main(String[] args) {
    String initialBoard = "Board:\n" +
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
    java.util.Scanner sc = new java.util.Scanner(new java.io.ByteArrayInputStream(initialBoard.getBytes()));
    board.Board board = BoardParser.parse(sc, config);
    System.out.println(board.getHeight() + "x" + board.getWidth());
    System.out.println(board.getPieceAt(0,0));
    System.out.println(board.getPieceAt(7,0));
  }
}
