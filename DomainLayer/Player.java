package DomainLayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Objects;

public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private EnumMap<ChipType, Integer> chips;
    private ArrayList<Card> cards;

    public Player() {
        this("Player");
    }

    public Player(String name) {
        this.name = name;
        chips = new EnumMap<>(ChipType.class);
        for (ChipType t : ChipType.values()) {
            chips.put(t, 0);
        }
        cards = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void takeSameChips(ChipType chip) {
        Objects.requireNonNull(chip);
        chips.put(chip, chips.get(chip) + 2);
    }

    public void takeDifferentChips(ChipType c1, ChipType c2, ChipType c3) {
        if (c1 == c2 || c1 == c3 || c2 == c3) {
            throw new IllegalArgumentException();
        }
        chips.put(c1, chips.get(c1) + 1);
        chips.put(c2, chips.get(c2) + 1);
        chips.put(c3, chips.get(c3) + 1);
    }

    public EnumMap<ChipType, Integer> getChips() {
        return new EnumMap<>(chips);
    }

    public boolean buyCard(Card card) {
        EnumMap<ChipType, Integer> costMap = new EnumMap<>(ChipType.class);

        for (ChipType t : card.cost) {
            costMap.put(t, costMap.getOrDefault(t, 0) + 1);
        }

        for (ChipType t : costMap.keySet()) {
            if (chips.get(t) < costMap.get(t)) {
                return false;
            }
        }

        for (ChipType t : costMap.keySet()) {
            chips.put(t, chips.get(t) - costMap.get(t));
        }

        cards.add(card);
        return true;
    }

    public int getVictoryPoints() {
        return cards.stream().mapToInt(c -> c.pointValue).sum();
    }

    public ArrayList<Card> getCards() {
        return new ArrayList<>(cards);
    }
}
