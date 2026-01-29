package DomainLayer;

import DataStorageLayer.*;
import java.util.ArrayList;

/**
 * GameController manages the core game logic and rules.
 * This class belongs in the Domain Layer and should have no UI dependencies.
 */
public class GameController {
    private static final int ROWS = 3;
    private static final int COLS = 5;

    private Card[][] board;
    private Player[] players;
    private int currentPlayerIndex;
    private ArrayList<Move> moveHistory;

    private DataLogger dataLogger;
    private LeaderboardManager leaderboardManager;
    private LeaderboardState leaderboardState;

    public GameController() {
        this.dataLogger = new DataLogger();
        this.leaderboardManager = new LeaderboardManager();
        this.leaderboardState = dataLogger.loadLeaderboard();
        this.moveHistory = new ArrayList<>();
    }

    /**
     * Initialize a new game with player names
     */
    public void startNewGame(String player1Name, String player2Name) {
        dataLogger.clearSavedGame();

        players = new Player[]{
                new Player(player1Name),
                new Player(player2Name)
        };
        currentPlayerIndex = 0;
        moveHistory = new ArrayList<>();

        board = new Card[ROWS][COLS];
        initializeBoard();

        saveGameState();
    }

    /**
     * Load a saved game, returns true if successful
     */
    public boolean loadSavedGame() {
        GameState saved = dataLogger.loadGame();
        if (saved != null) {
            this.board = saved.board;
            this.players = saved.players;
            this.currentPlayerIndex = saved.currentPlayerIndex;
            this.moveHistory = (saved.moves != null) ? saved.moves : new ArrayList<>();
            return true;
        }
        return false;
    }

    /**
     * Initialize the board with cards
     */
    private void initializeBoard() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int points = r + 1;
                ArrayList<ChipType> cost = generateCost(points);
                board[r][c] = new Card(cost, points);
            }
        }
    }

    /**
     * Generate a cost for a card based on its point value
     */
    private ArrayList<ChipType> generateCost(int points) {
        ArrayList<ChipType> cost = new ArrayList<>();
        ChipType[] colors = ChipType.values();
        ChipType primary = colors[(int) (Math.random() * colors.length)];

        // Add primary color (at least 2)
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
                    next = colors[(int) (Math.random() * colors.length)];
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

    /**
     * Attempt to buy a card at the given position
     * @return Error message if failed, null if successful
     */
    public String attemptBuyCard(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return "Invalid card position";
        }

        Card card = board[row][col];
        if (card == null) {
            return "No card at this position";
        }

        Player currentPlayer = players[currentPlayerIndex];

        if (!currentPlayer.buyCard(card)) {
            return "Not enough chips to buy this card";
        }

        // Purchase successful
        moveHistory.add(new Move(
                Move.Type.BUY_CARD,
                currentPlayerIndex,
                "Bought " + card.pointValue + "VP card from row " + (row + 1)
        ));

        board[row][col] = null;
        saveGameState();

        // Check if game is over
        if (isBoardEmpty()) {
            recordGameResult();
            return null; // Success, game over will be detected by isGameOver()
        }

        nextTurn();
        return null; // Success
    }

    /**
     * Take two chips of the same color
     * @return Error message if failed, null if successful
     */
    public String takeTwoSameChips(ChipType chip) {
        if (chip == null) {
            return "Must select a chip color";
        }

        Player currentPlayer = players[currentPlayerIndex];
        currentPlayer.takeSameChips(chip);

        moveHistory.add(new Move(
                Move.Type.TAKE_TWO_SAME,
                currentPlayerIndex,
                "Took 2 " + chip
        ));

        saveGameState();
        nextTurn();
        return null; // Success
    }

    /**
     * Take three chips of different colors
     * @return Error message if failed, null if successful
     */
    public String takeThreeDifferentChips(ChipType c1, ChipType c2, ChipType c3) {
        if (c1 == null || c2 == null || c3 == null) {
            return "Must select three chip colors";
        }

        if (c1 == c2 || c1 == c3 || c2 == c3) {
            return "All three chips must be different colors";
        }

        try {
            Player currentPlayer = players[currentPlayerIndex];
            currentPlayer.takeDifferentChips(c1, c2, c3);

            moveHistory.add(new Move(
                    Move.Type.TAKE_THREE_DIFF,
                    currentPlayerIndex,
                    "Took 3 different chips: " + c1 + ", " + c2 + ", " + c3
            ));

            saveGameState();
            nextTurn();
            return null; // Success
        } catch (IllegalArgumentException e) {
            return "Invalid chip selection";
        }
    }

    /**
     * Advance to the next player's turn
     */
    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
        saveGameState();
    }

    /**
     * Save the current game state
     */
    private void saveGameState() {
        GameState state = new GameState(board, players, currentPlayerIndex, moveHistory);
        dataLogger.saveGame(state);
    }

    /**
     * Check if the board is empty (game over condition)
     */
    private boolean isBoardEmpty() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] != null) return false;
            }
        }
        return true;
    }

    /**
     * Record the game result to the leaderboard
     */
    private void recordGameResult() {
        GameRecord record = new GameRecord();
        record.player1Name = players[0].getName();
        record.player2Name = players[1].getName();

        record.p1VP = players[0].getVictoryPoints();
        record.p2VP = players[1].getVictoryPoints();

        // Determine winner
        if (record.p1VP > record.p2VP) {
            record.winnerIndex = 0;
            record.winnerVP = record.p1VP;
            record.margin = record.p1VP - record.p2VP;
        } else if (record.p2VP > record.p1VP) {
            record.winnerIndex = 1;
            record.winnerVP = record.p2VP;
            record.margin = record.p2VP - record.p1VP;
        } else {
            record.winnerIndex = -1; // Tie
            record.winnerVP = record.p1VP;
            record.margin = 0;
        }

        // Count buys and high-row buys
        record.p1BuyCount = players[0].getCards().size();
        record.p2BuyCount = players[1].getCards().size();

        record.p1HighRowBuys = (int) players[0].getCards().stream()
                .filter(c -> c.pointValue >= 3).count();
        record.p2HighRowBuys = (int) players[1].getCards().stream()
                .filter(c -> c.pointValue >= 3).count();

        if (record.winnerIndex == 0) {
            record.winnerBuyCount = record.p1BuyCount;
            record.loserBuyCount = record.p2BuyCount;
            record.winnerHighRowBuys = record.p1HighRowBuys;
            record.loserHighRowBuys = record.p2HighRowBuys;
        } else if (record.winnerIndex == 1) {
            record.winnerBuyCount = record.p2BuyCount;
            record.loserBuyCount = record.p1BuyCount;
            record.winnerHighRowBuys = record.p2HighRowBuys;
            record.loserHighRowBuys = record.p1HighRowBuys;
        }

        // Generate analysis
        record.analysisText = leaderboardManager.analyze(record);

        // Update leaderboard
        leaderboardState = leaderboardManager.updateLeaderboard(leaderboardState, record);

        // Update win counts and best VP
        String name0 = players[0].getName();
        String name1 = players[1].getName();

        leaderboardState.bestVP.put(name0,
                Math.max(leaderboardState.bestVP.getOrDefault(name0, 0), record.p1VP));
        leaderboardState.bestVP.put(name1,
                Math.max(leaderboardState.bestVP.getOrDefault(name1, 0), record.p2VP));

        if (record.winnerIndex != -1) {
            String winnerName = players[record.winnerIndex].getName();
            leaderboardState.wins.put(winnerName,
                    leaderboardState.wins.getOrDefault(winnerName, 0) + 1);
        }

        // Save leaderboard
        dataLogger.saveLeaderboard(leaderboardState);
    }

    // Getter methods for the Presentation Layer

    public Card[][] getBoard() {
        return board;
    }

    public Player[] getPlayers() {
        return players;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public Player getCurrentPlayer() {
        return players[currentPlayerIndex];
    }

    public boolean isGameOver() {
        return isBoardEmpty();
    }

    public int getWinnerIndex() {
        if (!isGameOver()) return -1;

        int p1Points = players[0].getVictoryPoints();
        int p2Points = players[1].getVictoryPoints();

        if (p1Points > p2Points) return 0;
        if (p2Points > p1Points) return 1;
        return -1; // Tie
    }

    public LeaderboardState getLeaderboardState() {
        return leaderboardState;
    }

    public int getRows() {
        return ROWS;
    }

    public int getCols() {
        return COLS;
    }
}