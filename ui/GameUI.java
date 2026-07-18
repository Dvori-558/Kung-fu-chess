package ui;

import board.Board;
import board.Controller;
import engine.GameEngine;
import engine.GameEngineImpl;
import engine.GameSnapshot;
import models.Piece;
import rules.GameConfig;
import utils.CoordinateConverter;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Swing dashboard-style UI: center board, side move logs, score and player labels. */
public class GameUI extends JPanel {
    private final GameEngine engine;
    private final Controller controller;
    private final CoordinateConverter converter;
    private final GameConfig config;

    private final SpriteAssetCatalog assets;
    private final GameRenderer renderer;
    private final VisualSnapshotFactory visualSnapshotFactory;

    private final BoardPanel boardPanel;
    private final JLabel topScoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
    private final JLabel bottomScoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
    private final JLabel topNameLabel = new JLabel("Name: White", SwingConstants.CENTER);
    private final JLabel bottomNameLabel = new JLabel("Name: Black", SwingConstants.CENTER);
    private final DefaultTableModel blackMovesModel = new DefaultTableModel(new Object[]{"Time", "Move"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final DefaultTableModel whiteMovesModel = new DefaultTableModel(new Object[]{"Time", "Move"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private long uiTimeMs = 0L;
    private String lastUiMessage = "";
    private int whiteScore = 0;
    private int blackScore = 0;
    private Piece[][] previousBoardState;
    private final MoveObservable moveObservable = new MoveObservable();

    public GameUI(GameEngine engine, Controller controller, GameConfig config) {
        super(new BorderLayout(16, 16));
        this.engine = engine;
        this.controller = controller;
        this.config = config;
        this.converter = new CoordinateConverter(config.getPixelsPerCell());

        int boardCols = engine.snapshot().getBoard().getWidth();
        int boardRows = engine.snapshot().getBoard().getHeight();
        this.assets = new SpriteAssetCatalog(config.getPixelsPerCell());
        this.renderer = new GameRenderer(config, assets, boardCols, boardRows);
        this.visualSnapshotFactory = new VisualSnapshotFactory(engine, controller, config.getPixelsPerCell(), assets);
        this.boardPanel = new BoardPanel();
        this.previousBoardState = copyBoard(engine.snapshot().getBoard());

        setBackground(new Color(126, 126, 126));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildMainArea(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        setupInput();
        setupObservers();

        Timer timer = new Timer(30, e -> {
            uiTimeMs += 30L;
            engine.pause(30);
            updateFromState();
            repaint();
        });
        timer.start();

        updateFromState();
    }

    private void setupObservers() {
        moveObservable.addObserver(event -> {
            DefaultTableModel model = event.moverColor == Piece.WHITE ? whiteMovesModel : blackMovesModel;
            model.addRow(new Object[]{formatClock(uiTimeMs), event.moveLabel});
        });

        moveObservable.addObserver(event -> {
            if (event.capturedPiece == null) return;
            int value = pieceValue(event.capturedPiece);
            if (event.moverColor == Piece.WHITE) {
                whiteScore += value;
            } else {
                blackScore += value;
            }
            topScoreLabel.setText("Score: " + whiteScore);
            bottomScoreLabel.setText("Score: " + blackScore);
        });
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        topNameLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        topScoreLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        panel.add(topNameLabel, BorderLayout.NORTH);
        panel.add(topScoreLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildBottomBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        bottomScoreLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        bottomNameLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        panel.add(bottomScoreLabel, BorderLayout.NORTH);
        panel.add(bottomNameLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildMainArea() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        panel.add(buildMoveLogPanel(true), BorderLayout.WEST);
        panel.add(boardPanel, BorderLayout.CENTER);
        panel.add(buildMoveLogPanel(false), BorderLayout.EAST);
        return panel;
    }

    private JPanel buildMoveLogPanel(boolean blackSide) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setPreferredSize(new Dimension(170, 10));
        panel.setBackground(new Color(190, 190, 190));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JLabel title = new JLabel(blackSide ? "Black" : "White", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setOpaque(true);
        title.setBackground(Color.WHITE);
        title.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(title, BorderLayout.NORTH);

        JTable table = new JTable(blackSide ? blackMovesModel : whiteMovesModel);
        table.setRowHeight(22);
        table.getTableHeader().setReorderingAllowed(false);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void setupInput() {
        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (engine.isGameOver()) {
                    lastUiMessage = "המשחק הסתיים - לא ניתן לבצע מהלכים נוספים";
                    return;
                }

                if (SwingUtilities.isLeftMouseButton(e)) {
                    int boardX = e.getX() - boardPanel.getBoardOffsetX();
                    int boardY = e.getY() - boardPanel.getBoardOffsetY();
                    controller.click(boardX, boardY);
                    lastUiMessage = toUiMessage(controller.getLastMoveReason());
                    updateFromState();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    int boardX = e.getX() - boardPanel.getBoardOffsetX();
                    int boardY = e.getY() - boardPanel.getBoardOffsetY();
                    controller.jump(boardX, boardY);
                    lastUiMessage = toUiMessage(controller.getLastMoveReason());
                    updateFromState();
                }
                repaint();
            }
        });
    }

    private void updateFromState() {
        GameSnapshot snapshot = engine.snapshot();
        observeBoard(snapshot.getBoard());

        if (snapshot.isGameOver()) {
            Character winner = null;
            if (engine instanceof GameEngineImpl) {
                winner = ((GameEngineImpl) engine).getWinnerColor();
            }
            if (winner == Piece.WHITE) {
                lastUiMessage = "Winner: White";
            } else if (winner == Piece.BLACK) {
                lastUiMessage = "Winner: Black";
            } else {
                lastUiMessage = "No more moves";
            }
        }

        topScoreLabel.setText("Score: " + whiteScore);
        bottomScoreLabel.setText("Score: " + blackScore);
    }

    private void observeBoard(Board board) {
        if (previousBoardState == null) {
            previousBoardState = copyBoard(board);
            return;
        }

        List<CellDiff> diffs = collectDiffs(previousBoardState, board);
        if (diffs.isEmpty()) {
            return;
        }

        MoveEvent event = detectMoveEvent(board, diffs);
        previousBoardState = copyBoard(board);
        if (event != null) {
            moveObservable.publish(event);
        }
    }

    private MoveEvent detectMoveEvent(Board board, List<CellDiff> diffs) {
        List<CellDiff> sourceCandidates = new ArrayList<>();
        List<CellDiff> destCandidates = new ArrayList<>();

        for (CellDiff diff : diffs) {
            if (diff.before != null && (diff.after == null || !samePiece(diff.before, diff.after))) {
                sourceCandidates.add(diff);
            }
            if (diff.after != null && (diff.before == null || !samePiece(diff.before, diff.after))) {
                destCandidates.add(diff);
            }
        }

        if (sourceCandidates.isEmpty() || destCandidates.isEmpty()) {
            return null;
        }

        CellDiff dest = destCandidates.get(0);
        Piece mover = dest.after;
        if (mover == null) {
            return null;
        }

        CellDiff source = null;
        for (CellDiff c : sourceCandidates) {
            if (c.before != null && c.before.getColor() == mover.getColor()) {
                source = c;
                break;
            }
        }
        if (source == null) {
            source = sourceCandidates.get(0);
        }

        Piece captured = null;
        if (dest.before != null && dest.before.getColor() != mover.getColor()) {
            captured = dest.before;
        }

        String moveLabel = pieceLetter(mover) + toSquare(board, source.row, source.col)
                + (captured == null ? "-" : "x")
                + toSquare(board, dest.row, dest.col);

        return new MoveEvent(mover.getColor(), moveLabel, captured);
    }

    private List<CellDiff> collectDiffs(Piece[][] before, Board current) {
        List<CellDiff> diffs = new ArrayList<>();
        for (int row = 0; row < current.getHeight(); row++) {
            for (int col = 0; col < current.getWidth(); col++) {
                Piece prev = before[row][col];
                Piece now = current.getPieceAt(row, col);
                if (!samePiece(prev, now)) {
                    diffs.add(new CellDiff(row, col, prev, now));
                }
            }
        }
        return diffs;
    }

    private Piece[][] copyBoard(Board board) {
        Piece[][] copy = new Piece[board.getHeight()][board.getWidth()];
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                copy[row][col] = board.getPieceAt(row, col);
            }
        }
        return copy;
    }

    private boolean samePiece(Piece a, Piece b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private String pieceLetter(Piece piece) {
        String name = piece.getType().getName();
        if ("Knight".equalsIgnoreCase(name)) return "N";
        return name.substring(0, 1).toUpperCase();
    }

    private String toSquare(Board board, int row, int col) {
        char file = (char) ('a' + col);
        int rank = board.getHeight() - row;
        return "" + file + rank;
    }

    private String formatClock(long ms) {
        long totalSeconds = ms / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        long millis = ms % 1000L;
        return String.format("%02d:%02d.%03d", minutes, seconds, millis);
    }

    private int pieceValue(Piece piece) {
        String name = piece.getType().getName();
        Map<String, Integer> values = new HashMap<>();
        values.put("Pawn", 1);
        values.put("Knight", 3);
        values.put("Bishop", 3);
        values.put("Rook", 5);
        values.put("Queen", 9);
        values.put("King", 0);
        return values.getOrDefault(name, 0);
    }

    private String toUiMessage(String reason) {
        if (reason == null || reason.isEmpty()) return "";
        String key = reason.trim().toLowerCase();
        if (key.startsWith("jump rejected: outside board")) return "לא ניתן לקפוץ מחוץ לגבולות הלוח";
        if (key.startsWith("jump rejected")) return "הקפיצה נדחתה";
        if (key.equals("outside_board")) return "המהלך מחוץ לגבולות הלוח";
        if (key.equals("empty_source")) return "אין כלי במשבצת שנבחרה";
        if (key.equals("friendly_destination")) return "לא ניתן לאכול כלי מאותו צבע";
        if (key.equals("illegal_piece_move")) return "המהלך לא חוקי עבור הכלי שנבחר";
        if (key.equals("motion_in_progress")) return "יש מהלך פעיל כרגע - נסי שוב בעוד רגע";
        if (key.equals("rest_in_progress")) return "הכלי במנוחה כרגע - נסי שוב בעוד רגע";
        if (key.equals("game_over")) return "המשחק הסתיים";
        if (key.equals("ok")) return "";
        return "המהלך נדחה";
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        updateFromState();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension boardSize = renderer.getPreferredSize();
        return new Dimension(boardSize.width + 480, boardSize.height + 120);
    }

    public static void launch(GameEngine engine, Controller controller, GameConfig config) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kung-fu Chess");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            GameUI ui = new GameUI(engine, controller, config);
            frame.setContentPane(ui);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private final class BoardPanel extends JPanel {
        private int boardOffsetX = 0;
        private int boardOffsetY = 0;

        private BoardPanel() {
            setOpaque(false);
            setPreferredSize(renderer.getPreferredSize());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension boardSize = renderer.getPreferredSize();
            boardOffsetX = Math.max(0, (getWidth() - boardSize.width) / 2);
            boardOffsetY = Math.max(0, (getHeight() - boardSize.height) / 2);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(boardOffsetX, boardOffsetY);
            VisualSnapshot snapshot = visualSnapshotFactory.build(uiTimeMs);
            renderer.render(g2, uiTimeMs, lastUiMessage, snapshot);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return renderer.getPreferredSize();
        }

        private int getBoardOffsetX() {
            return boardOffsetX;
        }

        private int getBoardOffsetY() {
            return boardOffsetY;
        }
    }

    private static final class CellDiff {
        private final int row;
        private final int col;
        private final Piece before;
        private final Piece after;

        private CellDiff(int row, int col, Piece before, Piece after) {
            this.row = row;
            this.col = col;
            this.before = before;
            this.after = after;
        }
    }

    private static final class MoveEvent {
        private final char moverColor;
        private final String moveLabel;
        private final Piece capturedPiece;

        private MoveEvent(char moverColor, String moveLabel, Piece capturedPiece) {
            this.moverColor = moverColor;
            this.moveLabel = moveLabel;
            this.capturedPiece = capturedPiece;
        }
    }

    private interface MoveObserver {
        void onMove(MoveEvent event);
    }

    private static final class MoveObservable {
        private final List<MoveObserver> observers = new ArrayList<>();

        private void addObserver(MoveObserver observer) {
            observers.add(observer);
        }

        private void publish(MoveEvent event) {
            SwingUtilities.invokeLater(() -> {
                for (MoveObserver observer : observers) {
                    observer.onMove(event);
                }
            });
        }
    }
}