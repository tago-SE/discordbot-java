package commands;

import db.UsersController;
import db.models.BNetUser;
import db.models.GameType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageUtils;

import java.awt.*;

public class StatsCommand implements CommandInt {

    private static final String TAG = "stats";

    private static final String PLAYER_NOT_FOUND    = "Player {%s} not found.";
    private static final String STATS_FORMAT        = "Wins: %d\nLosses: %d\nRank: %s\nW/L: %s%%\nK/D: %s";

    private static double ratio(int n, int m) {
        if (m == 0) {
            if (n != 0)
                return 1;
            return 0;
        }
        return (double) n/m;
    }

    private static String formatStats(BNetUser.Stats stats) {
        return String.format(STATS_FORMAT,
                stats.wins,
                stats.losses,
                (stats.rank > 0)? stats.rank : "none",
                String.format("%.1f", ratio(stats.wins, stats.wins + stats.losses)*100),
                String.format("%.1f", ratio(stats.kills, stats.deaths)));
    }

    @Override
    public boolean publicCommand() {
        return true;
    }


    @Override
    public void execute(String[] args, GuildMessageReceivedEvent ev) {
        TextChannel channel = ev.getChannel();
        if (args.length > 1) {
            BNetUser u = UsersController.findByName(args[1]);
            if (u != null) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(u.name);
                eb.addField("FFA", formatStats(u.stats(GameType.FFA)), true);
                eb.addField("Team", formatStats(u.stats(GameType.TEAM)), true);
                eb.addField("Solo", formatStats(u.stats(GameType.SOLO)), true);
                eb.setColor(Color.YELLOW);
                channel.sendMessage(eb.build()).queue();
            } else {
                channel.sendMessage(MessageUtils.error(String.format(PLAYER_NOT_FOUND, args[1]))).queue();
            }
        }
    }

    @Override
    public String tag() {
        return TAG;
    }

    @Override
    public String help() {
        return TAG + " [user]";
    }
}
