package db.models.settings;

public class FogSettings {

    public static final int NONE    = 0;
    public static final int FULL    = 1;
    public static final int NIGHT   = 2;
    public static final int PARTIAL = 3;

    public static boolean validate(int type) {
        return type == NONE || type == FULL || type == NIGHT || type == PARTIAL;
    }
}