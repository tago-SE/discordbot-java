import db.DBHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import utils.ConfigManager;

import javax.security.auth.login.LoginException;

public class Main {

    public static void main(String[] args) throws LoginException {

        // Initialize configuration
        ConfigManager.initialize();

        // Enable Bot
        String token = ConfigManager.getDiscordToken();
        JDA jda = new JDABuilder(token).build();
        jda.addEventListener(new EventHandler());

        // Connect to database
        DBHandler.connect();


    }


}
