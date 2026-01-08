package DataStorageLayer;

import java.io.Serializable;
import java.util.ArrayList;

public class LeaderboardState implements Serializable {
    private static final long serialVersionUID = 1L;
    public ArrayList<GameRecord> records = new ArrayList<>();
}
