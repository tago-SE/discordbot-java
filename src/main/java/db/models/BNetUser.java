package db.models;

import java.util.Comparator;

public class BNetUser {

    public static class Stats {
        public int wins;
        public int losses;
        public int kills;
        public int deaths;
        public int rank;

        @Override
        public String toString() {
            return "Stats{" +
                    "wins=" + wins +
                    ", losses=" + losses +
                    ", kills=" + kills +
                    ", deaths=" + deaths +
                    ", rank=" + rank +
                    '}';
        }

        private int compare(Stats other) {
            if (other.wins == this.wins)
                return this.losses - other.losses;
            return other.wins - this.wins;
        }
    }

    public Stats ffa = new Stats();
    public Stats solo = new Stats();
    public Stats team = new Stats();

    public Stats stats(String gameType) {
        switch (gameType) {
            case "ffa": return ffa;
            case "team": return team;
            case "solo": return solo;
        }
        throw new IllegalArgumentException("gameType=" + gameType);
    }

    public String name;
    public String discordUser;

    public BNetUser() { }

    public BNetUser(String name) {
        this.name = name;
    }

    /**
     * Contains various comparators to sort users according to their rank in different leagues. Currently rank is
     * determined exclusively by number of wins.
     */
    public static class Comparators  {

        public static Comparator<BNetUser> SOLO = new Comparator<BNetUser>() {
            @Override
            public int compare(BNetUser o1, BNetUser o2) {
                return o1.stats(GameType.SOLO).compare(o2.stats(GameType.SOLO));
            }
        };

        public static Comparator<BNetUser> TEAM = new Comparator<BNetUser>() {
            @Override
            public int compare(BNetUser o1, BNetUser o2) {
                return o1.stats(GameType.TEAM).compare(o2.stats(GameType.TEAM));
            }
        };

        public static Comparator<BNetUser> FFA = new Comparator<BNetUser>() {
            @Override
            public int compare(BNetUser o1, BNetUser o2) {
                return o1.stats(GameType.FFA).compare(o2.stats(GameType.FFA));
            }
        };
    }

    @Override
    public String toString() {
        return "BNetUser{" +
                "ffa=" + ffa +
                ", solo=" + solo +
                ", team=" + team +
                ", name='" + name + '\'' +
                ", discordUser='" + discordUser + '\'' +
                '}';
    }
}
