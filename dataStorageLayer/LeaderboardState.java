package DataStorageLayer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LeaderboardState implements Serializable {
    private static final long serialVersionUID = 1L;

    public Map<String, Integer> wins = new HashMap<>();
    public Map<String, Integer> bestVP = new HashMap<>();
}
