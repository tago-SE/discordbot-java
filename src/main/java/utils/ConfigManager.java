package utils;

import utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private static final String CONFIG_PATH = "config.txt";

    private static boolean initialized = false;

    private static final int TOKEN_INDEX    = 0;
    private static final int VER_INDEX      = 1;
    private static final int MAP_INDEX      = 2;
    private static final int ADMIN_INDEX    = 3;
    private static final int SUPER_INDEX    = 4;


    public static String discordToken = null;
    public static final List<String> validMaps = new ArrayList<>();
    public static final List<String> validVersions = new ArrayList<>();
    public static final List<String> adminUsers = new ArrayList<>();
    public static final List<String> superUsers = new ArrayList<>();


    public static void initialize() {
        if (initialized)
            return;
        initialized = true;
        load();
        System.out.println("Config initialization: " + discordToken + " " + validMaps + " " + validVersions + " " + adminUsers + " " + superUsers);
    }

    public static String getDiscordToken() {
        return discordToken;
    }

    public static void addMap(String map) {
        validMaps.add(map);
        save();
    }

    public static void removeMap(String map) {
        validMaps.remove(map);
        save();
    }

    public static void addVersion(String version) {
        validVersions.add(version);
        save();
        System.out.println(validVersions);
    }

    public static void removeVersion(String ver) {
        validVersions.remove(ver);
        save();
    }

    private static void load() {
        Reader inputString = null;
        BufferedReader reader = null;
        try {
            String content = FileUtils.readText(CONFIG_PATH);
            inputString = new StringReader(content);
            reader = new BufferedReader(inputString);
            int index = 0;
            while (true) {
                String line = reader.readLine();
                if (line == null || line.length() == 0)
                    break;
                if (line.equals("#")) {
                    index++;
                } else {
                    switch (index) {
                        case TOKEN_INDEX: discordToken = line;
                            break;
                        case VER_INDEX: validVersions.add(line);
                            break;
                        case MAP_INDEX: validMaps.add(line);
                            break;
                        case ADMIN_INDEX: adminUsers.add(line);
                            break;
                        case SUPER_INDEX: superUsers.add(line);
                        default:
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputString != null) try {
                inputString.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (reader != null) try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void save() {
        StringBuilder sb = new StringBuilder();
        sb.append(discordToken).append("\n#\n");
        for (String ver : validVersions)
            sb.append(ver).append("\n");
        sb.append("#\n");
        for (String map : validMaps)
            sb.append(map).append("\n");
        sb.append("#\n");
        for (String admin : adminUsers)
            sb.append(admin).append("\n");
        sb.append("#\n");
        for (String superAdmin : superUsers)
            sb.append(superAdmin).append("\n");
        sb.append("#\n");
        FileUtils.writeText(sb.toString(), CONFIG_PATH);
    }

}
