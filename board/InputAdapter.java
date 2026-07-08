package board;

import engine.*;

public class InputAdapter {
    public Command parse(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 0) return null;
        String cmd = parts[0];
        try {
            if (cmd.equals("click") && parts.length >= 3) {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                return new ClickCommand(x,y);
            } else if (cmd.equals("jump") && parts.length >= 3) {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                return new JumpCommand(x,y);
            } else if (cmd.equals("wait") && parts.length >= 2) {
                int ms = Integer.parseInt(parts[1]);
                return new WaitCommand(ms);
            } else if (cmd.equals("print") && parts.length >= 2 && parts[1].equals("board")) {
                return new PrintBoardCommand();
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }
}
