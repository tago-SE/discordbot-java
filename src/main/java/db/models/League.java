package db.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class League {

    private static final int MAX_RANKED_USERS = 100;
    private static final int UNRANKED = 0;

    private final String leagueKey;
    private List<BNetUser> rankedUsers = new ArrayList<>();

    public League(String leagueKey) {
        this.leagueKey = leagueKey;
    }

    public List<BNetUser> getRankedUsers() {
        return rankedUsers;
    }

    public List<BNetUser> update(List<BNetUser> users) {
        BNetUser.Stats stats;
        List<BNetUser> changedUsers = new ArrayList<>();
        for (BNetUser user : users) {
            stats = user.stats(leagueKey);
            if (stats.wins > 0 && !rankedUsers.contains(user)) {
                rankedUsers.add(user);
            }
        }
        switch (leagueKey) {
            case GameType.FFA: rankedUsers.sort(BNetUser.Comparators.FFA); break;
            case GameType.SOLO: rankedUsers.sort(BNetUser.Comparators.SOLO); break;
            case GameType.TEAM: rankedUsers.sort(BNetUser.Comparators.TEAM); break;
            default: throw new IllegalStateException("Invalid: leagueKey=" + leagueKey);
        }
        int rank = 1;
        Iterator<BNetUser> itr = rankedUsers.iterator();
        while (itr.hasNext()) {
            BNetUser u = itr.next();
            stats = u.stats(leagueKey);
            if (stats.rank != rank) {
                changedUsers.add(u);
            }
            if (rank <= MAX_RANKED_USERS) {
                stats.rank = rank++;
            }
            else {
                stats.rank = UNRANKED;
                itr.remove();
            }
        }
        return changedUsers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("League{leagueKey='").append(leagueKey).append("',");
        for (BNetUser u : rankedUsers) {
            sb.append("{").append(u.stats(leagueKey).rank).append(":").append(u.name).append("}");
        }
        sb.append("}");
        return sb.toString();
    }


}
