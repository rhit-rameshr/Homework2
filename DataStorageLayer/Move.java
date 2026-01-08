package DataStorageLayer;

import java.io.Serializable;

public class Move implements Serializable {
    public enum Type { TAKE_TWO_SAME, TAKE_THREE_DIFF, BUY_CARD }

    public final Type type;
    public final int playerIndex;
    public final String detail;   // e.g. "Took 2 RED" or "Bought 3VP card (row 3)"

    public Move(Type type, int playerIndex, String detail) {
        this.type = type;
        this.playerIndex = playerIndex;
        this.detail = detail;
    }
}
