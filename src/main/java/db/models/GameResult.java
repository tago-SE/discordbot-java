package db.models;

@Deprecated // should be moved to "Replay"
public class GameResult {
    public static final String VICTORY      = "win";
    public static final String DEFEAT       = "lose";
    public static final String DRAW         = "draw";
    public static final String OBSERVER     = "obs";

    public static boolean validate(String s) {
        return s.equals(VICTORY) || s.equals(DEFEAT) || s.equals(DRAW) || s.equals(OBSERVER);
    }
}
