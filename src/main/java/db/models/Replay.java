package db.models;

import java.util.*;

public class Replay {

    public int gameId;
    public String map;
    public String version;
    public boolean rankedMatch;
    public int turns;
    public int fogMode;
    public int length;
    public String gameType;
    public int activePlayers;
    public int hash;
    public Date date;

    public List<ReplayPlayer> players = new ArrayList<>();

    /**
     * This will determine the game category, either single player, 1vs1, team or ffa as well as count how many
     * active players were in the game.
     */
    public void update() {
        activePlayers = 0;
        gameType = GameType.FFA; // default
        HashMap<Integer, List<ReplayPlayer>> teamsHashMap = new HashMap<>();
        for (ReplayPlayer player : players) {
            if (!player.isObserver()) {
                activePlayers++;
                if (player.team < 0)
                    continue;
                List<ReplayPlayer> team = teamsHashMap.get(player.team);
                if (team == null) {
                    team = new ArrayList<>();
                    teamsHashMap.put(player.team, team);
                }
                team.add(player);
                if (team.size() > 1)
                    gameType = GameType.TEAM; // if more than one player is on the same team game type is changed
            }
        }
        if (activePlayers <= 1)
            gameType = "single player";
        else if (activePlayers == 2 && teamsHashMap.size() > 1)
            gameType = GameType.SOLO;
    }


    public boolean isRankedMatch() {
        return rankedMatch;
    }

    public void sortPlayersByTeam() {
        Collections.sort(players, ReplayPlayer.Comparators.TEAM);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, turns, fogMode, players);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Replay replay = (Replay) o;
        return rankedMatch == replay.rankedMatch &&
                turns == replay.turns &&
                fogMode == replay.fogMode &&
                Objects.equals(version, replay.version) &&
                Objects.equals(gameType, replay.gameType) &&
                Objects.equals(players, replay.players);
    }

    public boolean containsPlayer(String name) {
        name = name.toLowerCase();
        for (ReplayPlayer p : players) {
            if (name.equals(p.name.toLowerCase()))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Replay{" +
                "map='" + map + '\'' +
                ", version='" + version + '\'' +
                ", rankedMatch=" + rankedMatch +
                ", turns=" + turns +
                ", fogMode=" + fogMode +
                ", length=" + length +
                ", gameType='" + gameType + '\'' +
                ", activePlayers=" + activePlayers +
                ", hash=" + hash +
                ", players=" + players +
                '}';
    }
}
