import commands.*;
import commands.admin.MapCommand;
import commands.admin.ShowMapsCommand;
import commands.admin.ShowVersionCommand;
import commands.admin.VersionCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EventHandler extends ListenerAdapter {
    private static final String CMD_PREFIX          = "!";
    private static final String CHANNEL_GENERAL     = "general";
    private List<CommandInt> commands = null;

    public EventHandler() {
        commands = new ArrayList<>();
        commands.add(new RankCommand());
        commands.add(new StatsCommand());
        commands.add(new SubmitCommand());
        commands.add(new SearchUsersCommand());
        commands.add(new VersionCommand());
        commands.add(new MapCommand());
        commands.add(new ShowMapsCommand());
        commands.add(new ShowVersionCommand());
        commands.add(new SearchGamesCommand());
        commands.add(new ShowCommands(CMD_PREFIX, commands));
    }

    public void onGuildMessageReceived(GuildMessageReceivedEvent ev) {
        User author = ev.getAuthor();
        Message message = ev.getMessage();
        String[] args = message.getContentRaw().split(" ");
        TextChannel channel = ev.getChannel();

        // Ignore bots or messages sent in the wrong channel
        if (author.isBot() || !channel.getName().equals(CHANNEL_GENERAL))
            return;

        if (!message.getAttachments().isEmpty()) {
            //onFileUpload(message.getAttachments().get(0), channel);
            return;
        }
        // Ignore invalid command prefixes
        if (!args[0].substring(0, 1).equals(CMD_PREFIX))
            return;
        // Execute command
        for (CommandInt cmd : commands) {
            if ((CMD_PREFIX + cmd.tag()).equals(args[0])) {
                cmd.execute(args, ev);
                return;
            }
        }
    }

    // TODO: Not yet implemented
    private static void onFileUpload(Message.Attachment attachment, TextChannel channel) {
        // api.wc3stats.com/upload
        String fileName = attachment.getFileName();
        if (fileName.substring(fileName.length() - 3).equals("w3g")) {

            File file = new File(fileName);
            attachment.downloadToFile(file);

            System.out.println("path: " + file.getAbsolutePath());
            System.out.println("name: " + file.getName());
            System.out.println("read: " + file.canRead());
            /*
            if (file.canRead()) {
                System.out.println("Can be read");
            } else {
                System.out.println("cannot be read");
            }
            try {
               // HttpHelper.postFile(file, "https://api.wc3stats.com/upload");
                channel.sendMessage("Uploading: " + file.toString()).queue();
            } catch (IOException e) {
                e.printStackTrace();
                channel.sendMessage(e.getMessage()).queue();
            }
            */
            System.out.println("Deleted");
            file.delete();

        } else {
            channel.sendMessage("Invalid file type.").queue();
        }
    }
}
