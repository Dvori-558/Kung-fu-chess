package texttests;

/**
 * ScriptParser parses one command line from the text integration DSL.
 *
 * Supported commands:
 * - click x y
 * - wait ms / pause ms
 * - print board
 */
public class ScriptParser {
    public ParsedCommand parse(String line) {
        if (line == null) {
            return ParsedCommand.empty();
        }

        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return ParsedCommand.empty();
        }

        if (trimmed.equals("print board")) {
            return ParsedCommand.printBoard();
        }

        String[] parts = trimmed.split("\\s+");
        String command = parts[0];

        if ("click".equals(command) && parts.length >= 3) {
            Integer x = parseInt(parts[1]);
            Integer y = parseInt(parts[2]);
            if (x != null && y != null) {
                return ParsedCommand.click(x, y);
            }
            return ParsedCommand.unknown();
        }

        if (("wait".equals(command) || "pause".equals(command)) && parts.length >= 2) {
            Long durationMs = parseLong(parts[1]);
            if (durationMs != null) {
                return ParsedCommand.waitMs(durationMs);
            }
            return ParsedCommand.unknown();
        }

        return ParsedCommand.unknown();
    }

    private Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static class ParsedCommand {
        public enum Type {
            EMPTY,
            CLICK,
            WAIT,
            PRINT_BOARD,
            UNKNOWN
        }

        private final Type type;
        private final int x;
        private final int y;
        private final long durationMs;

        private ParsedCommand(Type type, int x, int y, long durationMs) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.durationMs = durationMs;
        }

        public static ParsedCommand empty() {
            return new ParsedCommand(Type.EMPTY, 0, 0, 0L);
        }

        public static ParsedCommand unknown() {
            return new ParsedCommand(Type.UNKNOWN, 0, 0, 0L);
        }

        public static ParsedCommand click(int x, int y) {
            return new ParsedCommand(Type.CLICK, x, y, 0L);
        }

        public static ParsedCommand waitMs(long durationMs) {
            return new ParsedCommand(Type.WAIT, 0, 0, durationMs);
        }

        public static ParsedCommand printBoard() {
            return new ParsedCommand(Type.PRINT_BOARD, 0, 0, 0L);
        }

        public Type getType() {
            return type;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public long getDurationMs() {
            return durationMs;
        }
    }
}