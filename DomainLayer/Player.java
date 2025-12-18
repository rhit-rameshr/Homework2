package DomainLayer;

import java.util.ArrayList;
import java.util.EnumMap;

public class Player {

    private ArrayList<ChipType> chips;
    private ArrayList<Card> cards;

    public Player() {
        this.chips = new ArrayList<>();
        this.cards = new ArrayList<>();
    }

    public boolean buyCard(Card card) {

        // Count player's chips
        EnumMap<ChipType, Integer> chipCounts = new EnumMap<>(ChipType.class);
        for (ChipType chip : ChipType.values()) {
            chipCounts.put(chip, 0);
        }
        for (ChipType chip : chips) {
            chipCounts.put(chip, chipCounts.get(chip) + 1);
        }

        // Count required chips
        EnumMap<ChipType, Integer> costCounts = new EnumMap<>(ChipType.class);
        for (ChipType chip : ChipType.values()) {
            costCounts.put(chip, 0);
        }
        for (ChipType chip : card.cost) {
            costCounts.put(chip, costCounts.get(chip) + 1);
        }

        // Check availability
        for (ChipType chip : ChipType.values()) {
            if (chipCounts.get(chip) < costCounts.get(chip)) {
                return false;
            }
        }

        // Deduct chips
        for (ChipType chip : card.cost) {
            chips.remove(chip);
        }

        cards.add(card);
        return true;
    }


    public void takeSameChips(ChipType chip1, ChipType chip2) {
        chips.add(chip1);
        chips.add(chip2);
    }

    public void takeDifferentChips(ChipType chip1, ChipType chip2, ChipType chip3) {
        chips.add(chip1);
        chips.add(chip2);
        chips.add(chip3);
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public ArrayList<ChipType> getChips() {
        return chips;
    }
}
