package commands.admin;

import commands.CommandInt;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.ConfigManager;
import utils.MessageUtils;

public class VersionCommand implements CommandInt {

    private static final String TAG = "ver";

    private static final String VALID_VER_ADDED = "Version added: {%s}";
    private static final String VALID_VER_REMOVED = "Version removed: {%s}";
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
        if (args.length > 1) {
            String ver = ev.getMessage().getContentRaw().substring(args[0].length() + 1);
            if (ConfigManager.validVersions.contains(ver)) {
                channel.sendMessage(String.format(VALID_VER_REMOVED, ver)).queue();
                ConfigManager.removeVersion(ver);
            } else {
                channel.sendMessage(String.format(VALID_VER_ADDED, ver)).queue();
                ConfigManager.addVersion(ver);
            }
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
        return TAG + " [version]";
    }
}
