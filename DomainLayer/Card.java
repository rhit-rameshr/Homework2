package DomainLayer;

import java.io.Serializable;
import java.util.ArrayList;

public class Card implements Serializable {
    private static final long serialVersionUID = 1L;

    public ArrayList<ChipType> cost;
    public int pointValue;

    public Card(ArrayList<ChipType> cost, int pointValue) {
        this.cost = cost;
        this.pointValue = pointValue;
    }
}
