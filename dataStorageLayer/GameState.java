package DataStorageLayer;

import DomainLayer.Card;
import DomainLayer.Player;
import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    public Card[][] board;
    public Player[] players;
    public int currentPlayerIndex;
    public ArrayList<Move> moves;


    public GameState(Card[][] board, Player[] players, int currentPlayerIndex) {
        this.board = board;
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
    }
}
