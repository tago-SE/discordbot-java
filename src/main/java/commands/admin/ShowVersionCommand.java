package commands.admin;

import commands.CommandInt;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.ConfigManager;
import utils.MessageUtils;

public class ShowVersionCommand implements CommandInt {

    private static final String TAG = "versions";
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
        channel.sendMessage(ConfigManager.validVersions.toString()).queue();
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
        return TAG;
    }
}