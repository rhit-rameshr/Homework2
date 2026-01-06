package presentationLayer;

import DomainLayer.*;
import DataStorageLayer.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.EnumMap;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

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

    private JTextArea leaderboardArea;
    private JPanel leaderboardPanel;
    private LeaderboardState leaderboard;



    private DataLogger logger;
    private enum ChipActionMode {
        NONE,
        TAKE_TWO_SAME,
        TAKE_THREE_DIFFERENT
    }

    private ChipActionMode chipMode = ChipActionMode.NONE;
    private ArrayList<ChipType> selectedChips = new ArrayList<>();

    private final java.util.Map<Integer, ImageIcon> cardImageCache = new java.util.HashMap<>();


    public Main() {
        players = new Player[] { new Player(), new Player() };
        logger = new DataLogger();
        leaderboard = logger.loadLeaderboard();


        GameState loaded = logger.loadGame();
        if (loaded != null) {
            board = loaded.board;
            players = loaded.players;
            currentPlayer = loaded.currentPlayerIndex;
        } else {
            startNewGame();
        }

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
        logger.clearSavedGame();
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
        players[currentPlayer].takeSameChips(chip);
        nextTurn();
    }

    private void attemptBuyCard(int row, int col) {
        Card card = board[row][col];
        if (card == null) return;

        Player player = players[currentPlayer];

        if (player.buyCard(card)) {
            board[row][col] = null;

            if (isBoardEmpty()) {
                updateUIState();
                recordGameResult();
                showGameOverDialog();
                return;
            }


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
private ImageIcon getCardIcon(Card card) {
    int key = card.pointValue;

    // Return cached icon if already loaded
    if (cardImageCache.containsKey(key)) {
        return cardImageCache.get(key);
    }

    try {
        // Local image files (you can add more as needed)
        String[] localFiles = {
            "resources/img1.jpg",
            "resources/img2.jpg",
            "resources/img3.jpg"
        };

        // Map pointValue to an image deterministically
        String path = localFiles[(key - 1) % localFiles.length];

        // Load image from resources
        BufferedImage img = ImageIO.read(getClass().getResource(path));

        // Scale image to fit card
        Image scaled = img.getScaledInstance(90, 70, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaled);

        // Cache and return
        cardImageCache.put(key, icon);
        return icon;

    } catch (Exception e) {
        e.printStackTrace();
        return null; // fallback: no image
    }
}


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

        JButton takeTwoSameBtn = new JButton("Take 2 Same Chips");
        takeTwoSameBtn.addActionListener(e -> {
            chipMode = ChipActionMode.TAKE_TWO_SAME;
            selectedChips.clear();
        });
        controlPanel.add(takeTwoSameBtn);

        JButton takeThreeDiffBtn = new JButton("Take 3 Different Chips");
        takeThreeDiffBtn.addActionListener(e -> {
            chipMode = ChipActionMode.TAKE_THREE_DIFFERENT;
            selectedChips.clear();
        });
        controlPanel.add(takeThreeDiffBtn);

        /* Individual chip buttons */
        for (ChipType chip : ChipType.values()) {
            JButton chipButton = new JButton(chip.toString());
            chipButton.addActionListener(e -> handleChipSelection(chip));
            controlPanel.add(chipButton);
        }

        JButton restartButton = new JButton("Restart Game");
        restartButton.addActionListener(e -> restartGame());
        controlPanel.add(restartButton);

        add(controlPanel, BorderLayout.SOUTH);

        setSize(1000, 600);
        setVisible(true);

        leaderboardArea = new JTextArea();
        leaderboardArea.setEditable(false);

        leaderboardPanel = new JPanel(new BorderLayout());
        leaderboardPanel.add(new JLabel("Leaderboard", SwingConstants.CENTER), BorderLayout.NORTH);
        leaderboardPanel.add(new JScrollPane(leaderboardArea), BorderLayout.CENTER);

        add(leaderboardPanel, BorderLayout.EAST);

    }

    private void endChipAction() {
        chipMode = ChipActionMode.NONE;
        selectedChips.clear();
        nextTurn();
    }

    private void handleChipSelection(ChipType chip) {
        if (chipMode == ChipActionMode.NONE) {
            JOptionPane.showMessageDialog(
                    this,
                    "Choose a chip action first.",
                    "No Action Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (chipMode == ChipActionMode.TAKE_TWO_SAME) {
            selectedChips.add(chip);

            if (selectedChips.size() == 2) {
                players[currentPlayer].takeSameChips(selectedChips.get(0));
                endChipAction();
            }

        } else if (chipMode == ChipActionMode.TAKE_THREE_DIFFERENT) {
            if (selectedChips.contains(chip)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Chips must be different.",
                        "Invalid Selection",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            selectedChips.add(chip);

            if (selectedChips.size() == 3) {
                players[currentPlayer]
                        .takeDifferentChips(
                                selectedChips.get(0),
                                selectedChips.get(1),
                                selectedChips.get(2)
                        );
                endChipAction();
            }
        }
    }   

    private boolean isBoardEmpty() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] != null) {
                    return false;
                }
            }
        }
        return true;
    }

    private int getWinner() {
        int p1Points = calculatePoints(players[0]);
        int p2Points = calculatePoints(players[1]);

        if (p1Points > p2Points) return 0;
        if (p2Points > p1Points) return 1;
        return -1; // tie
    }
    private void recordGameResult() {
        String[] names = {"Player 1", "Player 2"};

        int winner = getWinner();
        int p0 = players[0].getVictoryPoints();
        int p1 = players[1].getVictoryPoints();


        leaderboard.bestVP.put(names[0], Math.max(leaderboard.bestVP.getOrDefault(names[0], 0), p0));
        leaderboard.bestVP.put(names[1], Math.max(leaderboard.bestVP.getOrDefault(names[1], 0), p1));

        if (winner != -1) {
            String wName = names[winner];
            leaderboard.wins.put(wName, leaderboard.wins.getOrDefault(wName, 0) + 1);
        }

        logger.saveLeaderboard(leaderboard);
    }


    private void showGameOverDialog() {
        int winner = getWinner();

        String message;

        if (winner == -1) {
            message = "The game is a tie!";
        } else {
            message = "Player " + (winner + 1) + " wins!";
        }

        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Restart Game", "Exit"},
                "Restart Game"
        );

        if (choice == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            System.exit(0);
        }
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

        updateLeaderboardUI();


        turnIndicator.setText("Current Turn: Player " + (currentPlayer + 1));

        boardPanel.revalidate();
        boardPanel.repaint();
        logger.saveGame(new GameState(board, players, currentPlayer));
    }



private JPanel createCardView(int row, int col) {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    panel.setLayout(new BorderLayout());

    Card card = board[row][col];

    if (card != null) {
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(90, 70));
        imageLabel.setIcon(getCardIcon(card)); // use local image

        JLabel points = new JLabel("⭐ " + card.pointValue, SwingConstants.CENTER);
        JLabel cost = new JLabel("Cost: " + card.cost, SwingConstants.CENTER);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.add(points);
        textPanel.add(cost);

        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(textPanel, BorderLayout.SOUTH);

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

        EnumMap<ChipType, Integer> chips = player.getChips();

        for (ChipType chip : ChipType.values()) {
            chipCount.put(chip, chips.getOrDefault(chip, 0));
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

    private void updateLeaderboardUI() {
        String[] names = {"Player 1", "Player 2"};

        StringBuilder sb = new StringBuilder();
        sb.append("HISTORIC LEADERBOARD\n\n");

        for (String name : names) {
            int wins = leaderboard.wins.getOrDefault(name, 0);
            int best = leaderboard.bestVP.getOrDefault(name, 0);

            sb.append(name)
                    .append(" | Wins: ").append(wins)
                    .append(" | Best VP: ").append(best)
                    .append("\n");
        }

        sb.append("\nCurrent Turn: ").append(names[currentPlayer]);
        leaderboardArea.setText(sb.toString());
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
