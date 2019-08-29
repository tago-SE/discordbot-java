package commands;

import db.ReplaysController;
import db.UsersController;
import db.models.BNetUser;
import db.models.GameType;
import db.models.Replay;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageUtils;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class SearchGamesCommand implements CommandInt {

    private static final String TAG                     = "games";
    private static final String NO_USERNAME             = "You need to specify a username";
    private static final String PLAYERS_NOT_FOUND       = "No players found matching {%s}";

    private static final String TITLE_COL_0             = "Replay";
    private static final String TITLE_COL_1             = "Result";
    private static final String TITLE_COL_2             = "Date";
    private static final String TEAM_RESULT_TITLE       = "Team (%s)";
    private static final String SOLO_RESULT_TITLE       = "Solo (%s)";
    private static final String FFA_RESULT_TITLE        = "FFA (%s)";

    @Override
    public boolean publicCommand() {
        return true;
    }

    private static String formatReplayTitle(Replay replay) {
        if (GameType.isTeam(replay.gameType))
            return String.format(TEAM_RESULT_TITLE, (replay.isRankedMatch()? "Ranked" : "Unranked"));
        if (GameType.isSolo(replay.gameType))
            return String.format(SOLO_RESULT_TITLE, (replay.isRankedMatch()? "Ranked" : "Unranked"));
        if (GameType.isFFA(replay.gameType))
            return String.format(FFA_RESULT_TITLE, (replay.isRankedMatch()? "Ranked" : "Unranked"));
        return "Invalid Game Type";
    }

    @Override
    public void execute(String[] args, GuildMessageReceivedEvent ev) {
        TextChannel channel = ev.getChannel();
        if (args.length < 2) {
            channel.sendMessage(MessageUtils.error(NO_USERNAME)).queue();
            return;
        }
        String name = args[1];
        BNetUser user = UsersController.findByName(name);
        if (user != null) {
            List<Replay> replays = ReplaysController.findReplaysByPlayer(name);
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(user.name);
            StringBuilder sb1 = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            StringBuilder sb3 = new StringBuilder();
            for (Replay replay : replays) {
                // Replay header
                sb1.append("[").append("#").append(replay.gameId).append(" ").append(formatReplayTitle(replay)).append("](")
                        .append("https://wc3stats.com/games/").append(replay.gameId).append(")\n");
                // Result
                sb2.append(replay.players.get(0).result).append("\n");
                // Date
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                sb3.append(dateFormat.format(replay.date)).append("\n");
            }
            eb.addField(TITLE_COL_0, sb1.toString(), true);
            eb.addField(TITLE_COL_1, sb2.toString(), true);
            eb.addField(TITLE_COL_2, sb3.toString(), true);
            eb.setColor(Color.YELLOW);
            channel.sendMessage(eb.build()).queue();
        } else {
            channel.sendMessage(MessageUtils.error(String.format(PLAYERS_NOT_FOUND, args[1]))).queue();
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
