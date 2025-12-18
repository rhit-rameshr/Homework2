package dataStorageLayer;

import DomainLayer.Card;
import DomainLayer.Player;

import java.io.FileWriter;
import java.io.IOException;

public class DataLogger {

    public String fileName;

    public DataLogger(String fileName) {
        this.fileName = fileName;
    }

    public void logData(Player[] players, Card[] availableCards) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write("Game State:\n");

            for (int i = 0; i < players.length; i++) {
                writer.write("Player " + i + " has " +
                        players[i].getCards().size() + " cards\n");
            }

            writer.write("Available cards: " + availableCards.length + "\n");
            writer.write("----------------------\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
