package presentationLayer;

import DomainLayer.Card;
import DomainLayer.Player;
import dataStorageLayer.DataLogger;

public class Main {

    private Card[] board;
    private Player[] players;
    private DataLogger logger;

    public Main() {
        players = new Player[] { new Player(), new Player() };
        logger = new DataLogger("game_log.txt");
        startNewGame();
    }

    private void startNewGame() {
        board = new Card[0]; // placeholder
        updateBoard();
    }

    private void updateBoard() {
        // Update available cards on the board
    }

    private void nextTurn() {
        // Advance to next player's turn
    }

    private void saveGameState() {
        logger.logData(players, board);
    }

    private boolean checkIfGameOver() {
        // Simple placeholder logic
        return false;
    }

    public static void main(String[] args) {
        new Main();
    }
}
