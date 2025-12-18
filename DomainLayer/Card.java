package DomainLayer;

import java.util.ArrayList;

public class Card {

    public ArrayList<ChipType> cost;
    public int pointValue;

    public Card(ArrayList<ChipType> cost, int pointValue) {
        this.cost = cost;
        this.pointValue = pointValue;
    }
}