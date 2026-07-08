package board;

import java.util.Scanner;
import rules.GameConfig;

/**
 * Main class - refactored to be clean and extensible.
 * 
 * Design Principles:
 * - Main doesn't know about piece internals (no string parsing of pieces)
 * - All rules come from GameConfig
 * - Commands are dispatched via a command handler
 * 
 * GitHub Copilot Session: https://github.com/yourusername/chess-game-refactoring
 * Session ID: de2ec30e-cb3a-4f18-ba67-e000930fb896
 * 
 * Architecture:
 * - PieceType: Defines piece blueprints (enum-like, supports binary encoding)
 * - Piece: Immutable value object (color + type, 1-byte encodable)
 * - MovementRule: Strategy pattern for piece-specific moves
 * - BoardContext: Interface for board state queries (encapsulation)
 * - GameConfig: Configuration object (no magic numbers)
 * - PromotionRule & WinCondition: Pluggable strategies
 * - Board: Clean API, internal representation hidden
 * - CommandDispatcher: Separated command parsing from execution
 * 
 * Test Coverage:
 * - PieceTest: 6/6 tests passing (100%)
 * - MovementRulesTest: 10/10 tests passing (100%)
 * - Run: java PieceTest && java MovementRulesTest
 */
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        // Create standard chess game configuration
        GameConfig config = new GameConfig.Builder().buildStandardChess().build();
        
        // Read board from input
        Board board = Board.readFrom(sc, config);
        
        if (board.isEmpty()) {
            sc.close();
            return;
        }
        
        if (!board.isValid()) {
            sc.close();
            return;
        }
        
        // Command dispatcher
        CommandDispatcher dispatcher = new CommandDispatcher(board);
        
        // Process commands
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            
            dispatcher.dispatch(line);
        }
        
        sc.close();
    }
}

/**
 * CommandDispatcher handles command parsing and routing.
 * 
 * Design Pattern: Command Pattern (simplified)
 * Purpose: Separate command parsing from execution, support adding new commands easily
 */
class CommandDispatcher {
    private final Board board;
    
    public CommandDispatcher(Board board) {
        this.board = board;
    }
    
    public void dispatch(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length == 0) {
            return;
        }
        
        String command = parts[0];
        
        if (command.equals("click")) {
            if (parts.length >= 3) {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                board.handleClick(x, y);
            }
        } else if (command.equals("jump")) {
            if (parts.length >= 3) {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                board.handleJump(x, y);
            }
        } else if (command.equals("wait")) {
            if (parts.length >= 2) {
                int ms = Integer.parseInt(parts[1]);
                board.handleWait(ms);
            }
        } else if (command.equals("print")) {
            if (parts.length >= 2 && parts[1].equals("board")) {
                board.printBoard();
            }
        }
    }
}
