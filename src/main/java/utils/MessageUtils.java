package utils;

public class MessageUtils {

    public static String error(String s) {
        return "```diff\n" +
                "- " + s + "\n" +
                "```";
    }
}
