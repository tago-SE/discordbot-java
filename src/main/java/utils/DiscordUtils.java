package utils;

import net.dv8tion.jda.api.entities.User;

public class DiscordUtils {

    public static boolean isAdmin(User user) {
        String userName = user.getName() + "#" + user.getDiscriminator();
        return ConfigManager.superUsers.contains(userName) || ConfigManager.adminUsers.contains(user.getName() + "#" + user.getDiscriminator());
    }

    public static boolean isSuperUser(User user) {
        return ConfigManager.adminUsers.contains(user.getName() + "#" + user.getDiscriminator());
    }
}
