package presentationLayer;

import DomainLayer.*;
import DataStorageLayer.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;

/**
 * Main UI class - Presentation Layer only
 * All game logic has been moved to GameController in the Domain Layer
 */
public class Main extends JFrame {

    private GameController gameController;

    private JPanel boardPanel;
    private JTextArea player1Info;
    private JTextArea player2Info;
    private JLabel turnIndicator;
    private JTextArea leaderboardArea;
    private JPanel leaderboardPanel;

    private enum ChipActionMode {
        NONE,
        TAKE_TWO_SAME,
        TAKE_THREE_DIFFERENT
    }

    private ChipActionMode chipMode = ChipActionMode.NONE;
    private final ArrayList<ChipType> selectedChips = new ArrayList<>();

    private final java.util.Map<Integer, ImageIcon> cardImageCache = new java.util.HashMap<>();

    public Main() {
        gameController = new GameController();

        setupUI();

        // Check if there's a saved game
        boolean hasSavedGame = gameController.loadSavedGame();

        if (hasSavedGame) {
            // Ask if they want to continue or start new
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "A saved game was found. Would you like to continue or start a new game?",
                    "Continue Game?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Continue", "New Game"},
                    "Continue"
            );

            if (choice == JOptionPane.NO_OPTION) {
                // User wants new game
                promptForNewGame();
            }
            // else: keep the loaded game
        } else {
            // No saved game, prompt for new
            promptForNewGame();
        }

        updateUIState();
    }

    /**
     * Prompt for player names and start new game
     */
    private void promptForNewGame() {
        String p1 = JOptionPane.showInputDialog(this, "Enter name for Player 1:");
        if (p1 == null || p1.isBlank()) p1 = "Player 1";

        String p2 = JOptionPane.showInputDialog(this, "Enter name for Player 2:");
        if (p2 == null || p2.isBlank()) p2 = "Player 2";

        gameController.startNewGame(p1, p2);
    }

    /**
     * Get card icon for display
     */
    private ImageIcon getCardIcon(Card card) {
        int key = card.pointValue;

        if (cardImageCache.containsKey(key)) {
            return cardImageCache.get(key);
        }

        try {
            String[] localFiles = {
                    "/resources/img1.jpg",
                    "/resources/img2.jpg",
                    "/resources/img3.jpg"
            };

            String path = localFiles[(key - 1) % localFiles.length];
            BufferedImage img = ImageIO.read(getClass().getResource(path));

            Image scaled = img.getScaledInstance(90, 70, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(scaled);

            cardImageCache.put(key, icon);
            return icon;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Setup the user interface
     */
    private void setupUI() {
        setTitle("Mini-Splendor Card Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Turn indicator at top
        turnIndicator = new JLabel("", SwingConstants.CENTER);
        turnIndicator.setFont(new Font("Arial", Font.BOLD, 16));
        add(turnIndicator, BorderLayout.NORTH);

        // Board in center
        boardPanel = new JPanel(new GridLayout(gameController.getRows(), gameController.getCols()));
        add(boardPanel, BorderLayout.CENTER);

        // Player info on left
        JPanel infoPanel = new JPanel(new GridLayout(1, 2));
        player1Info = new JTextArea();
        player2Info = new JTextArea();
        player1Info.setEditable(false);
        player2Info.setEditable(false);

        infoPanel.add(new JScrollPane(player1Info));
        infoPanel.add(new JScrollPane(player2Info));
        add(infoPanel, BorderLayout.WEST);

        // Control panel at bottom
        JPanel controlPanel = new JPanel();

        JButton takeTwoSameBtn = new JButton("Take 2 Same Chips");
        takeTwoSameBtn.addActionListener(e -> {
            chipMode = ChipActionMode.TAKE_TWO_SAME;
            selectedChips.clear();
            showMessage("Select one chip color (click twice on same color button)");
        });
        controlPanel.add(takeTwoSameBtn);

        JButton takeThreeDiffBtn = new JButton("Take 3 Different Chips");
        takeThreeDiffBtn.addActionListener(e -> {
            chipMode = ChipActionMode.TAKE_THREE_DIFFERENT;
            selectedChips.clear();
            showMessage("Select three different chip colors");
        });
        controlPanel.add(takeThreeDiffBtn);

        // Chip buttons
        for (ChipType chip : ChipType.values()) {
            JButton chipButton = new JButton(chip.toString());
            chipButton.addActionListener(e -> handleChipSelection(chip));
            controlPanel.add(chipButton);
        }

        JButton restartButton = new JButton("Restart Game");
        restartButton.addActionListener(e -> restartGame());
        controlPanel.add(restartButton);

        JButton viewLeaderboardBtn = new JButton("View Leaderboard");
        viewLeaderboardBtn.addActionListener(e -> showLeaderboardDialog());
        controlPanel.add(viewLeaderboardBtn);

        add(controlPanel, BorderLayout.SOUTH);

        // Leaderboard panel on right
        leaderboardArea = new JTextArea();
        leaderboardArea.setEditable(false);

        leaderboardPanel = new JPanel(new BorderLayout());
        leaderboardPanel.add(new JLabel("Leaderboard", SwingConstants.CENTER), BorderLayout.NORTH);
        leaderboardPanel.add(new JScrollPane(leaderboardArea), BorderLayout.CENTER);
        add(leaderboardPanel, BorderLayout.EAST);

        setSize(1200, 650);
        setVisible(true);
    }

    /**
     * Handle chip selection based on current mode
     */
    private void handleChipSelection(ChipType chip) {
        if (chipMode == ChipActionMode.NONE) {
            showError("Choose a chip action first (Take 2 Same or Take 3 Different)");
            return;
        }

        if (chipMode == ChipActionMode.TAKE_TWO_SAME) {
            selectedChips.add(chip);

            if (selectedChips.size() == 2) {
                String error = gameController.takeTwoSameChips(selectedChips.get(0));
                if (error != null) {
                    showError(error);
                    selectedChips.clear();
                } else {
                    endChipAction();
                    checkGameOver();
                }
            }

        } else if (chipMode == ChipActionMode.TAKE_THREE_DIFFERENT) {
            if (selectedChips.contains(chip)) {
                showError("Chips must be different - you already selected " + chip);
                return;
            }

            selectedChips.add(chip);

            if (selectedChips.size() == 3) {
                String error = gameController.takeThreeDifferentChips(
                        selectedChips.get(0),
                        selectedChips.get(1),
                        selectedChips.get(2)
                );
                if (error != null) {
                    showError(error);
                    selectedChips.clear();
                } else {
                    endChipAction();
                    checkGameOver();
                }
            }
        }
    }

    /**
     * End the current chip selection action
     */
    private void endChipAction() {
        chipMode = ChipActionMode.NONE;
        selectedChips.clear();
        updateUIState();
    }

    /**
     * Attempt to buy a card at the given position
     */
    private void attemptBuyCard(int row, int col) {
        String error = gameController.attemptBuyCard(row, col);

        if (error != null) {
            showError(error);
        } else {
            updateUIState();
            checkGameOver();
        }
    }

    /**
     * Check if game is over and show dialog
     */
    private void checkGameOver() {
        if (gameController.isGameOver()) {
            showGameOverDialog();
        }
    }

    /**
     * Restart the game
     */
    private void restartGame() {
        promptForNewGame();
        updateUIState();
    }

    /**
     * Show game over dialog
     */
    private void showGameOverDialog() {
        int winner = gameController.getWinnerIndex();
        Player[] players = gameController.getPlayers();

        String message;
        if (winner == -1) {
            message = "The game is a tie!\n" +
                    players[0].getName() + ": " + players[0].getVictoryPoints() + " VP\n" +
                    players[1].getName() + ": " + players[1].getVictoryPoints() + " VP";
        } else {
            message = players[winner].getName() + " wins with " +
                    players[winner].getVictoryPoints() + " victory points!";
        }

        // Show leaderboard info
        LeaderboardState lb = gameController.getLeaderboardState();
        if (!lb.records.isEmpty() && lb.records.get(0) != null) {
            GameRecord lastGame = lb.records.get(0);
            message += "\n\n" + lastGame.analysisText;
        }

        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"New Game", "Exit"},
                "New Game"
        );

        if (choice == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            System.exit(0);
        }
    }

    /**
     * Update all UI elements based on current game state
     */
    private void updateUIState() {
        // Update board
        boardPanel.removeAll();
        Card[][] board = gameController.getBoard();
        for (int r = 0; r < gameController.getRows(); r++) {
            for (int c = 0; c < gameController.getCols(); c++) {
                boardPanel.add(createCardView(r, c, board[r][c]));
            }
        }

        // Update player info
        Player[] players = gameController.getPlayers();
        int currentIdx = gameController.getCurrentPlayerIndex();

        updatePlayerInfo(player1Info, players[0], players[0].getName(), currentIdx == 0);
        updatePlayerInfo(player2Info, players[1], players[1].getName(), currentIdx == 1);

        // Update leaderboard
        updateLeaderboardUI();

        // Update turn indicator
        turnIndicator.setText("Current Turn: " + gameController.getCurrentPlayer().getName());

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    /**
     * Create a card view panel
     */
    private JPanel createCardView(int row, int col, Card card) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.setLayout(new BorderLayout());

        if (card != null) {
            JLabel imageLabel = new JLabel();
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setPreferredSize(new Dimension(90, 70));
            imageLabel.setIcon(getCardIcon(card));

            JLabel points = new JLabel("⭐ " + card.pointValue, SwingConstants.CENTER);
            JLabel cost = new JLabel("Cost: " + formatCost(card.cost), SwingConstants.CENTER);
            cost.setFont(new Font("Arial", Font.PLAIN, 10));

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

    /**
     * Format card cost for display
     */
    private String formatCost(ArrayList<ChipType> cost) {
        EnumMap<ChipType, Integer> counts = new EnumMap<>(ChipType.class);
        for (ChipType type : ChipType.values()) {
            counts.put(type, 0);
        }
        for (ChipType chip : cost) {
            counts.put(chip, counts.get(chip) + 1);
        }

        StringBuilder sb = new StringBuilder();
        for (ChipType type : ChipType.values()) {
            int count = counts.get(type);
            if (count > 0) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(type.toString().charAt(0)).append(count);
            }
        }
        return sb.toString();
    }

    /**
     * Update player info display
     */
    private void updatePlayerInfo(JTextArea area, Player player, String title, boolean isCurrent) {
        area.setText(title + (isCurrent ? "  ← TURN\n" : "\n"));
        area.append("Victory Points: " + player.getVictoryPoints() + "\n");
        area.append("Cards Bought: " + player.getCards().size() + "\n");
        area.append("\nChips:\n");

        EnumMap<ChipType, Integer> chips = player.getChips();
        for (ChipType chip : ChipType.values()) {
            area.append("  " + chip + ": " + chips.get(chip) + "\n");
        }

        area.setBackground(isCurrent ? new Color(220, 255, 220) : Color.WHITE);
    }

    /**
     * Update leaderboard display
     */
    private void updateLeaderboardUI() {
        LeaderboardState lb = gameController.getLeaderboardState();
        StringBuilder sb = new StringBuilder();
        sb.append("═══ LEADERBOARD ═══\n\n");

        // Show top 5 games
        if (!lb.records.isEmpty()) {
            sb.append("TOP SCORES:\n");
            for (int i = 0; i < Math.min(5, lb.records.size()); i++) {
                GameRecord rec = lb.records.get(i);
                String winner = rec.winnerIndex == 0 ? rec.player1Name :
                        rec.winnerIndex == 1 ? rec.player2Name : "TIE";
                sb.append((i + 1)).append(". ").append(winner)
                        .append(" - ").append(rec.winnerVP).append(" VP")
                        .append(" (margin: ").append(rec.margin).append(")\n");
            }
            sb.append("\n");
        }

        // Show player stats
        sb.append("PLAYER STATS:\n");
        java.util.Set<String> allPlayers = new java.util.HashSet<>();
        allPlayers.addAll(lb.wins.keySet());
        allPlayers.addAll(lb.bestVP.keySet());

        for (String name : allPlayers) {
            int wins = lb.wins.getOrDefault(name, 0);
            int best = lb.bestVP.getOrDefault(name, 0);
            sb.append(name)
                    .append("\n  Wins: ").append(wins)
                    .append(" | Best: ").append(best).append(" VP\n");
        }

        leaderboardArea.setText(sb.toString());
    }

    /**
     * Show leaderboard in dialog
     */
    private void showLeaderboardDialog() {
        LeaderboardState lb = gameController.getLeaderboardState();

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════\n");
        sb.append("     MINI-SPLENDOR LEADERBOARD\n");
        sb.append("═══════════════════════════════\n\n");

        if (lb.records.isEmpty()) {
            sb.append("No games recorded yet.\n");
        } else {
            sb.append("TOP 5 GAMES:\n");
            sb.append("───────────────────────────────\n");
            for (int i = 0; i < Math.min(5, lb.records.size()); i++) {
                GameRecord rec = lb.records.get(i);
                String winner = rec.winnerIndex == 0 ? rec.player1Name :
                        rec.winnerIndex == 1 ? rec.player2Name : "TIE";

                sb.append(String.format("#%d: %s - %d VP (Margin: %d)\n",
                        i + 1, winner, rec.winnerVP, rec.margin));
                sb.append("    " + rec.player1Name + " (" + rec.p1VP + ") vs " +
                        rec.player2Name + " (" + rec.p2VP + ")\n");
                sb.append("    " + rec.analysisText + "\n\n");
            }

            sb.append("\nALL-TIME PLAYER STATS:\n");
            sb.append("───────────────────────────────\n");
            java.util.Set<String> allPlayers = new java.util.HashSet<>();
            allPlayers.addAll(lb.wins.keySet());
            allPlayers.addAll(lb.bestVP.keySet());

            for (String name : allPlayers) {
                int wins = lb.wins.getOrDefault(name, 0);
                int best = lb.bestVP.getOrDefault(name, 0);
                sb.append(String.format("%-15s Wins: %2d | Best VP: %2d\n",
                        name, wins, best));
            }
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Leaderboard",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.WARNING_MESSAGE
        );
    }

    /**
     * Show informational message
     */
    private void showMessage(String message) {
        String originalText = turnIndicator.getText();
        turnIndicator.setText(message);
        Timer timer = new Timer(2000, e -> turnIndicator.setText(originalText));
        timer.setRepeats(false);
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}