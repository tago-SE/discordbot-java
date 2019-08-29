package db.models;

import java.util.Comparator;
import java.util.Objects;

public class ReplayPlayer  {

    public String name;
    public String result;
    public int rank;
    public int kills;
    public int deaths;
    public int eliminations;
    public int gold;
    public int team;
    public int apm;
    public int stayPercent;

    public boolean isObserver() {
        return result.equals("obs");
    }

    @Override
    public String toString() {
        return "ReplayPlayer{" +
                "name='" + name + '\'' +
                ", result='" + result + '\'' +
                ", rank=" + rank +
                ", kills=" + kills +
                ", deaths=" + deaths +
                ", gold=" + gold +
                ", team=" + team +
                ", apm=" + apm +
                ", stayPercent=" + stayPercent +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, result, rank, kills, deaths, gold, team);
    }


    public static class Comparators  {
        public static Comparator<ReplayPlayer> TEAM = Comparator.comparingInt(p -> p.team);
    }
}
