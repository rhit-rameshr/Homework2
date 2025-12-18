package presentationLayer;

import DomainLayer.*;
import DataStorageLayer.DataLogger;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.EnumMap;

public class Main extends JFrame {

    private static final int ROWS = 3;
    private static final int COLS = 5;

    private Card[][] board;
    private Player[] players;
    private int currentPlayer;

    private JPanel boardPanel;
    private JTextArea player1Info;
    private JTextArea player2Info;
    private JLabel turnIndicator;

    private DataLogger logger;

    public Main() {
        players = new Player[] { new Player(), new Player() };
        logger = new DataLogger("game_log.txt");

        startNewGame();
        setupUI();
        updateUIState();
    }

    /* ================= GAME LOGIC ================= */
    private ArrayList<ChipType> generateCost(int points) {
        ArrayList<ChipType> cost = new ArrayList<>();

        ChipType[] colors = ChipType.values();
        ChipType primary = colors[(int)(Math.random() * colors.length)];

        // At least two of one color
        cost.add(primary);
        cost.add(primary);

        int totalCost = points + 1;
        int remaining = totalCost - 2;

        int maxExtraColors = Math.min(2, colors.length - 1);
        ArrayList<ChipType> usedColors = new ArrayList<>();
        usedColors.add(primary);

        while (remaining > 0) {
            ChipType next;

            if (usedColors.size() < maxExtraColors + 1 && Math.random() > 0.5) {
                do {
                    next = colors[(int)(Math.random() * colors.length)];
                } while (usedColors.contains(next));
                usedColors.add(next);
            } else {
                next = primary;
            }

            cost.add(next);
            remaining--;
        }

        return cost;
    }

    private void startNewGame() {
        board = new Card[ROWS][COLS];
        currentPlayer = 0;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int points = r + 1; // Row-based difficulty (1–3)
                ArrayList<ChipType> cost = generateCost(points);
                board[r][c] = new Card(cost, points);
            }
        }
    }


    private void nextTurn() {
        currentPlayer = (currentPlayer + 1) % players.length;
        updateUIState();
    }

    private void takeChip(ChipType chip) {
        players[currentPlayer].takeSameChips(chip, chip);
        nextTurn();
    }

    private void attemptBuyCard(int row, int col) {
        Card card = board[row][col];
        if (card == null) return;

        Player player = players[currentPlayer];

        if (player.buyCard(card)) {
            board[row][col] = null;
            nextTurn();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Not enough chips to buy this card.",
                    "Purchase Failed",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void restartGame() {
        players = new Player[] { new Player(), new Player() };
        startNewGame();
        updateUIState();
    }

    /* ================= UI ================= */

    private void setupUI() {
        setTitle("Card Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        turnIndicator = new JLabel("", SwingConstants.CENTER);
        turnIndicator.setFont(new Font("Arial", Font.BOLD, 16));
        add(turnIndicator, BorderLayout.NORTH);

        boardPanel = new JPanel(new GridLayout(ROWS, COLS));
        add(boardPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new GridLayout(1, 2));
        player1Info = new JTextArea();
        player2Info = new JTextArea();

        player1Info.setEditable(false);
        player2Info.setEditable(false);

        infoPanel.add(new JScrollPane(player1Info));
        infoPanel.add(new JScrollPane(player2Info));
        add(infoPanel, BorderLayout.WEST);

        JPanel controlPanel = new JPanel();

        for (ChipType chip : ChipType.values()) {
            JButton chipButton = new JButton("Take " + chip);
            chipButton.addActionListener(e -> takeChip(chip));
            controlPanel.add(chipButton);
        }

        JButton restartButton = new JButton("Restart Game");
        restartButton.addActionListener(e -> restartGame());
        controlPanel.add(restartButton);

        add(controlPanel, BorderLayout.SOUTH);

        setSize(1000, 600);
        setVisible(true);
    }

    private void updateUIState() {
        boardPanel.removeAll();

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                boardPanel.add(createCardView(r, c));
            }
        }

        updatePlayerInfo(player1Info, players[0], "Player 1", currentPlayer == 0);
        updatePlayerInfo(player2Info, players[1], "Player 2", currentPlayer == 1);

        turnIndicator.setText("Current Turn: Player " + (currentPlayer + 1));

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private JPanel createCardView(int row, int col) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.setLayout(new BorderLayout());

        Card card = board[row][col];

        if (card != null) {
            JLabel points = new JLabel("Points: " + card.pointValue, SwingConstants.CENTER);
            JLabel cost = new JLabel("Cost: " + card.cost, SwingConstants.CENTER);

            panel.add(points, BorderLayout.NORTH);
            panel.add(cost, BorderLayout.CENTER);

            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            panel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    attemptBuyCard(row, col);
                }
            });
        } else {
            panel.add(new JLabel("Empty", SwingConstants.CENTER));
        }

        return panel;
    }

    private void updatePlayerInfo(
            JTextArea area,
            Player player,
            String title,
            boolean isCurrent
    ) {
        area.setText(title + (isCurrent ? "  ← TURN\n" : "\n"));
        area.append("Victory Points: " + calculatePoints(player) + "\n");
        area.append("Chips:\n");

        EnumMap<ChipType, Integer> chipCount = new EnumMap<>(ChipType.class);
        for (ChipType chip : ChipType.values()) {
            chipCount.put(chip, 0);
        }

        for (ChipType chip : player.getChips()) {
            chipCount.put(chip, chipCount.get(chip) + 1);
        }

        chipCount.forEach((k, v) ->
                area.append("  " + k + ": " + v + "\n"));

        area.setBackground(isCurrent ? new Color(220, 255, 220) : Color.WHITE);
    }

    private int calculatePoints(Player player) {
        return player.getCards()
                     .stream()
                     .mapToInt(c -> c.pointValue)
                     .sum();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
