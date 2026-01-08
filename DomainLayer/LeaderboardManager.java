package DomainLayer;

import DataStorageLayer.*;
import java.util.*;

public class LeaderboardManager {
    private final int MAX_ENTRIES = 5;

    public LeaderboardState updateLeaderboard(LeaderboardState lb, GameRecord record) {
        lb.records.add(record);
        lb.records.sort((a,b) -> {
            if (b.winnerVP != a.winnerVP) return b.winnerVP - a.winnerVP;
            return b.margin - a.margin;
        });
        if (lb.records.size() > MAX_ENTRIES) {
            lb.records = new ArrayList<>(lb.records.subList(0, MAX_ENTRIES));
        }
        return lb;
    }

    public boolean qualifies(LeaderboardState lb, GameRecord record) {
        // qualifies if it would end up in top N
        ArrayList<GameRecord> temp = new ArrayList<>(lb.records);
        temp.add(record);
        temp.sort((a,b) -> {
            if (b.winnerVP != a.winnerVP) return b.winnerVP - a.winnerVP;
            return b.margin - a.margin;
        });
        return temp.indexOf(record) < MAX_ENTRIES;
    }

    public boolean extraordinary(GameRecord record) {
        return record.margin >= 5 || record.winnerHighRowBuys >= 2;
    }

    public String analyze(GameRecord record) {
        // short, “I analyzed the game” sentence
        if (record.margin >= 5) {
            return "Big win: large VP margin likely from consistently higher-value purchases.";
        }
        if (record.winnerHighRowBuys > record.loserHighRowBuys) {
            return "Winner focused more on high-value cards, which created the gap.";
        }
        if (record.winnerBuyCount > record.loserBuyCount) {
            return "Winner bought more cards overall, compounding VP faster.";
        }
        return "Close game: similar buying pace and card values kept scores tight.";
    }
}
