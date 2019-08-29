package commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import utils.DiscordUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShowCommands implements CommandInt {

    public static final String TAG = "commands";
    private final String prefix;

    private final List<CommandInt> commands;

    public ShowCommands(String prefix, List<CommandInt> commands) {
        this.commands = commands;
        this.prefix = prefix;
    }

    @Override
    public boolean publicCommand() {
        return true;
    }

    @Override
    public void execute(String[] args, GuildMessageReceivedEvent ev) {
        StringBuilder sb = new StringBuilder();
        List<String> commandStrings = new ArrayList<>();
        for (CommandInt c : commands) {
            if (DiscordUtils.isAdmin(ev.getAuthor()) || c.publicCommand()) {
                commandStrings.add(c.help());
            }
        }
        Collections.sort(commandStrings);
        for (String s : commandStrings)
            sb.append(prefix).append(s).append("\n");
        ev.getChannel().sendMessage(sb.toString()).queue();
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
