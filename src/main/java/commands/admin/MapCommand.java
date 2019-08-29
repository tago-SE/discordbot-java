package commands.admin;

import commands.CommandInt;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.ConfigManager;
import utils.MessageUtils;

public class MapCommand implements CommandInt {

    private static final String TAG = "map";
    private static final String VALID_MAP_ADDED = "Map added: {%s}";
    private static final String VALID_MAP_REMOVED = "Map removed: {%s}";
    private static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
    
    @Override
    public void execute(String[] args, GuildMessageReceivedEvent ev) {
        TextChannel channel = ev.getChannel();
        User user = ev.getAuthor();
        String userName = user.getName() + "#" + user.getDiscriminator();
        if (!ConfigManager.superUsers.contains(userName) && !ConfigManager.adminUsers.contains(userName)) {
            channel.sendMessage(MessageUtils.error(UNAUTHORIZED_ACCESS)).queue();
            return;
        }
        String map = ev.getMessage().getContentRaw().substring(args[0].length() + 1);
        if (ConfigManager.validMaps.contains(map)) {
            channel.sendMessage(String.format(VALID_MAP_REMOVED, map)).queue();
            ConfigManager.removeMap(map);
        } else {
            channel.sendMessage(String.format(VALID_MAP_ADDED, map)).queue();
            ConfigManager.addMap(map);
        }
    }

    @Override
    public boolean publicCommand() {
        return false;
    }

    @Override
    public String tag() {
        return TAG;
    }

    @Override
    public String help() {
        return TAG + " [map name]";
    }
}