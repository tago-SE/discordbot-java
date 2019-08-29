package db.models;

@Deprecated // Should be moved to Replay
public final class GameType {

    public static final String TEAM = "team";
    public static final String SOLO = "solo";
    public static final String FFA = "ffa";

    public static final String[] values = {FFA, SOLO, TEAM};

    public static boolean isSolo(String s) {
        return s.equals(SOLO);
    }

    public static boolean isTeam(String s) {
        return s.equals(TEAM);
    }

    public static boolean isFFA(String s) {
        return s.equals(FFA);
    }

    public static boolean validate(String s) {
        return isSolo(s) || isTeam(s) || isFFA(s);
    }

}
