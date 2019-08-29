package db.models.settings;

public class ShipSettings {

    public static final int DEFAULT     = 0;
    public static final int WEAK_SHIPS  = 1;
    public static final int NO_SS       = 2;

    public static boolean validate(String s) {
        return s.equals(DEFAULT) || s.equals(WEAK_SHIPS) || s.equals(NO_SS);
    }
}
