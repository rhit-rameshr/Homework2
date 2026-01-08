package DataStorageLayer;

import java.io.Serializable;

public class GameRecord implements Serializable {
    public String player1Name;
    public String player2Name;

    public int winnerIndex;   // 0/1, -1 tie
    public int p1VP;
    public int p2VP;
    public int winnerVP;
    public int margin;

    // “analysis features”
    public int p1BuyCount;
    public int p2BuyCount;
    public int winnerBuyCount;
    public int loserBuyCount;

    public int p1HighRowBuys;
    public int p2HighRowBuys;
    public int winnerHighRowBuys;
    public int loserHighRowBuys;

    public String analysisText;
}
