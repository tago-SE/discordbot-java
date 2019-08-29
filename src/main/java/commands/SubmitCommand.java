package commands;

import db.ReplaysController;
import db.UsersController;
import db.models.BNetUser;
import db.models.GameType;
import db.models.Replay;
import db.models.ReplayPlayer;
import db.models.settings.FogSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.MessageUtils;

import java.awt.*;
import java.util.List;

public class SubmitCommand implements CommandInt {

    private static final  String TAG = "submit";

    public static final String TEAM_LEAGUE_TITLE        = "Risk Team League";
    public static final String SOLO_LEAGUE_TITLE        = "Risk Solo League";
    public static final String FFA_LEAGUE_TITLE         = "Risk FFA League";

    private static final String TEAM_RESULT_TITLE       = "Team Game (%s)";
    private static final String SOLO_RESULT_TITLE       = "Solo Game (%s)";
    private static final String FFA_RESULT_TITLE        = "FFA Game (%s)";

    private static final String FAILED_PARSE_ID         = "Failed to parse game id";
    private static final String REPLAY_ALREADY_UPLOADED = "Replay {%d} has already been submitted";
    private static final String INVALID_GAME_TYPE       = "Invalid game type {%s}";
    private static final String URL                     = "https://wc3stats.com/games/";

    private static final String SCOREBOARD_CHANNEL      = "scoreboard";
    private static final int SCOREBOARD_SIZE            = 25;

    private static String formatResultTitle(Replay replay) {
        if (GameType.isTeam(replay.gameType))
            return String.format(TEAM_RESULT_TITLE, (replay.isRankedMatch()? "Ranked" : "Unranked"));
        if (GameType.isSolo(replay.gameType))
            return String.format(SOLO_RESULT_TITLE, (replay.isRankedMatch()? "Ranked" : "Unranked"));
        if (GameType.isFFA(replay.gameType))
            return String.format(FFA_RESULT_TITLE, (replay.isRankedMatch()? "Ranked" : "Unranked"));
        return "Invalid Game Type";
    }

    private static String formatReplayPlayers(Replay replay) {
        StringBuilder sb = new StringBuilder();
        for (ReplayPlayer p : replay.players)
            if (!p.isObserver()) {
                // if team game append player team to name
                if (GameType.isTeam(replay.gameType))
                    sb.append("(").append(p.team).append(") ");
                sb.append(p.name).append("\n");
            }
        return sb.toString();
    }

    private static String formatReplayKillsDeaths(Replay replay) {
        StringBuilder sb = new StringBuilder();
        for (ReplayPlayer p : replay.players)
            if (!p.isObserver())
                sb.append(p.kills + "/" + p.deaths + "\n");
        return sb.toString();
    }

    private static String formatReplayResult(Replay replay) {
        StringBuilder sb = new StringBuilder();
        for (ReplayPlayer p : replay.players)
            if (!p.isObserver())
                sb.append(p.result  + "\n");
        return sb.toString();
    }

    private String formatFog(Replay replay) {
        switch (replay.fogMode) {
            case FogSettings.NONE: return "Fog off";
            case FogSettings.FULL: return "Fog on";
            case FogSettings.NIGHT: return "Night fog";
            case FogSettings.PARTIAL: return "Partial fog";
        }
        return "fog(null)";
    }

    private void displayReplayResult(Replay replay, TextChannel channel, int gameId) {
        // Debug message
        channel.sendMessage(replay.toString() + ". " + URL + gameId + ".\nHash: " + replay.hash + ".").queue();
        replay.sortPlayersByTeam();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(formatResultTitle(replay),  URL + gameId);
        eb.setColor(Color.YELLOW);
        eb.addField("Players", formatReplayPlayers(replay) + "\nTurns: " + replay.turns, true);
        eb.addField("Result", formatReplayResult(replay), true);
        eb.addField("K/D", formatReplayKillsDeaths(replay), true);
        eb.setDescription(formatFog(replay));
        channel.sendMessage(eb.build()).queue();
    }


    private void updateScoreboard(String gameType, TextChannel channel) {
        // Format
        String[] content = new String[4];
        List<BNetUser> players = UsersController.findAllRankedUsers(gameType);
        System.out.println("updateScoreboard: " + players);
        content[1] = content[2] = content[3] = "";
        int rank = 1;
        switch (gameType) {
            case GameType.TEAM: content[0] = TEAM_LEAGUE_TITLE; break;
            case GameType.FFA: content[0] = FFA_LEAGUE_TITLE; break;
            case GameType.SOLO: content[0] = SOLO_LEAGUE_TITLE; break;
        }
        for (BNetUser u : players) {
            BNetUser.Stats stats = u.stats(gameType);
            if (stats.wins == 0 || rank > SCOREBOARD_SIZE)
                break;
            content[1] += rank++ + ".\t" + u.name + "\n";
            content[2] += stats.wins + "\n";
            content[3] += stats.losses + "\n";
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.YELLOW);
        eb.setTitle(content[0]);
        eb.addField("Player", content[1], true);
        eb.addField("Wins", content[2], true);
        eb.addField("Losses", content[3], true);

        // Update
        TextChannel scoreChannel = channel.getGuild().getTextChannelsByName(SCOREBOARD_CHANNEL, true).get(0);

        /*  // Used to get message id
        channel.sendMessage("hello").queue(
                (success) -> {
                    System.out.println("ID: " + success.getId());
                }, (failure) -> {
                    failure.printStackTrace();
                }
        );
        */

        for (Message msg : new MessageHistory(scoreChannel).retrievePast(10).complete()) {
            if (msg.getEmbeds().get(0).getTitle().equals(content[0])) {
                scoreChannel.editMessageById(msg.getId(), eb.build()).queue();
                return;
            }
        }
        scoreChannel.sendMessage(eb.build()).queue();
    }

    @Override
    public void execute(String[] args, GuildMessageReceivedEvent ev) {
        TextChannel channel = ev.getChannel();
        int gameId;
        try {
            gameId = Integer.parseInt(args[1]);
        } catch (Exception e) {
            channel.sendMessage(MessageUtils.error(FAILED_PARSE_ID)).queue();
            return;
        }
        try {
            Replay replay = ReplaysController.fetchReplay("https://api.wc3stats.com/replays/" + gameId + "&toDisplay=true");
            if (!GameType.validate(replay.gameType)) {
                channel.sendMessage(MessageUtils.error(String.format(INVALID_GAME_TYPE, replay.gameType))).queue();
                return;
            }
            if (ReplaysController.gameAlreadySaved(gameId)) {
                channel.sendMessage(MessageUtils.error(String.format(REPLAY_ALREADY_UPLOADED, gameId))).queue();
                return;
            }
            displayReplayResult(replay, channel, gameId);
            if (replay.isRankedMatch()) {
                channel.sendMessage("Stats have been updated.").queue();
                UsersController.updateStats(replay);
                updateScoreboard(replay.gameType, channel);
                ReplaysController.insert(replay);
            }

        } catch (Exception e) {
            e.printStackTrace();
            channel.sendMessage(MessageUtils.error(e.getMessage())).queue();
        }
    }

    @Override
    public boolean publicCommand() {
        return true;
    }

    @Override
    public String tag() {
        return TAG;
    }

    @Override
    public String help() {
        return TAG + " [id]";
    }
}
