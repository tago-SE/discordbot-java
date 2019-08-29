package commands;

import db.UsersController;
import db.models.BNetUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageUtils;

import java.util.Iterator;
import java.util.List;

public class SearchUsersCommand implements CommandInt {

    private static final String TAG                     = "players";
    private static final String PLAYERS_NOT_FOUND       = "No players found matching {%s}";
    private static final String PLAYERS_FOUND           = "Players(%d): %s";

    @Override
    public boolean publicCommand() {
        return true;
    }

    @Override
    public void execute(String[] args, GuildMessageReceivedEvent ev) {
        TextChannel channel = ev.getChannel();
        List<BNetUser> foundUsers;
        if (args.length < 2)
            foundUsers = UsersController.findUsersByMatchingName("");
        else
            foundUsers = UsersController.findUsersByMatchingName(args[1]);

        if (foundUsers.isEmpty()) {
            channel.sendMessage(MessageUtils.error(String.format(PLAYERS_NOT_FOUND, args[1]))).queue();
        } else {
            StringBuilder sb = new StringBuilder();
            Iterator<BNetUser> itr = foundUsers.iterator();
            while (itr.hasNext()) {
                sb.append(itr.next().name);
                if (itr.hasNext())
                    sb.append(", ");
                else
                    sb.append(".");
            }
            channel.sendMessage(String.format(PLAYERS_FOUND, foundUsers.size(), sb.toString())).queue();
        }
    }

    @Override
    public String tag() {
        return TAG;
    }

    @Override
    public String help() {
        return TAG + " (user)";
    }
}
