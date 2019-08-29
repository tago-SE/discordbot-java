package commands;

import db.UsersController;
import db.models.BNetUser;
import db.models.GameType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageUtils;

public class RankCommand implements CommandInt {

    private static final  String TAG = "rank";

    private static final String INVALID_LEAGUE      = "Invalid league specified {%s}";
    private static final String PLAYER_NOT_FOUND    = "No user found matching {%s}";
    private static final String NO_ARGUMENTS        = "You need to specify league and username";
    private static final String NO_USERNAME         = "You need to specify a username";
    private static final String PLAYER_RANK         = "%s %s rank: %s";



    @Override
    public boolean publicCommand() {
        return true;
    }

    @Override
    public void execute(String[] args, GuildMessageReceivedEvent ev) {
        TextChannel channel = ev.getChannel();
        if (args.length > 2) {
            String league = args[1];
            if (!GameType.validate(league)) {
                channel.sendMessage(MessageUtils.error(String.format(INVALID_LEAGUE, league))).queue();
                return;
            }
            String name = args[2];
            BNetUser user = UsersController.findByName(name);
            if (user != null) {
                int userRank = user.stats(league).rank;
                channel.sendMessage(String.format(PLAYER_RANK, user.name, league, (userRank> 0 ? userRank: "none"))).queue();
            } else
                channel.sendMessage(MessageUtils.error(String.format(PLAYER_NOT_FOUND, name))).queue();
        }
        else {
            if (args.length == 1)
                channel.sendMessage(MessageUtils.error(String.format(NO_ARGUMENTS))).queue();
            else {
                String league = args[1];
                if (!GameType.validate(league))
                    channel.sendMessage(MessageUtils.error(String.format(INVALID_LEAGUE, league))).queue();
                else
                    channel.sendMessage(MessageUtils.error(NO_USERNAME)).queue();
            }
        }
    }

    @Override
    public String tag() {
        return TAG;
    }

    @Override
    public String help() {
        return TAG + " [league] [user]";
    }
}
