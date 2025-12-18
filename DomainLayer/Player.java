package DomainLayer;

import java.util.ArrayList;
import ChipType;

public class Player {

    private ArrayList<ChipType> chips;
    private ArrayList<Card> cards;

    public Player() {
        this.chips = new ArrayList<>();
        this.cards = new ArrayList<>();
    }

    public boolean buyCard(Card card) {
        if (chips.containsAll(card.cost)) {
            for (ChipType chip : card.cost) {
                chips.remove(chip);
            }
            cards.add(card);
            return true;
        }
        return false;
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
