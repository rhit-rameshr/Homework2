package DataStorageLayer;

import java.io.*;

public class DataLogger {

    private final String fileName = "gameState.dat";

    public void saveGame(GameState state) {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(state);
            System.out.println("Game state saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GameState loadGame() {
        File f = new File(fileName);
        if (!f.exists()) return null;

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(fileName))) {
            System.out.println("Game state loaded.");
            return (GameState) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clearSavedGame() {
        new File(fileName).delete();
    }
}
