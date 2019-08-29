package commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface CommandInt {


    boolean publicCommand();
    void execute(String[] args, GuildMessageReceivedEvent ev);
    String tag();
    String help();

}
