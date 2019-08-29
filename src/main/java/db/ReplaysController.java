package db;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import db.models.GameResult;
import db.models.Replay;
import db.models.ReplayPlayer;
import db.models.settings.FogSettings;
import org.bson.Document;
import utils.ConfigManager;
import utils.HttpHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReplaysController {

    // Replay fields
    private static final String MAP             = "map";
    private static final String VERSION         = "ver";
    private static final String TURNS           = "turns";
    private static final String RANKED          = "ranked";
    private static final String FOG             = "fog";
    private static final String LENGTH          = "length";
    private static final String PLAYER_COUNT    = "playing";
    private static final String GAME_TYPE       = "gameType";
    private static final String GAME_ID         = "gameId";
    private static final String PLAYERS         = "players";

    // ReplayPlayer fields
    private static final String NAME            = "name";
    private static final String RESULT          = "result";
    private static final String RANK            = "rank";
    private static final String KILLS           = "k";
    private static final String DEATHS          = "d";
    private static final String ELIMINATIONS    = "elim";
    private static final String GOLD            = "g";
    private static final String APM             = "apm";
    private static final String STAY_PERCENT    = "stay";
    private static final String TIMESTAMP       = "ts";

    private static MongoCollection<Document> col;
    private static boolean initialized = false;

    /*
    // Maximum number of cached players
    private static final int MAX_CACHED_PLAYERS = 100;
    // Cache containing a list with all replays belonging to a player, key used is the player name.
    private static final LinkedHashMap<String, List<Replay>> playerReplays
            = new LinkedHashMap<String, List<Replay>>() {
        protected boolean removeEldestEntry(Map.Entry<String, List<Replay>> eldest) {
            return size() > MAX_CACHED_PLAYERS;
        }
    };
    */

    private static final Replay toObject(Document doc) {
        Replay replay = new Replay();
        replay.gameId = doc.getInteger(GAME_ID);
        replay.gameType = doc.getString(GAME_TYPE);
        replay.date = doc.getDate(TIMESTAMP);
        replay.map = doc.getString(MAP);
        replay.version = doc.getString(VERSION);
        replay.turns = doc.getInteger(TURNS);
        replay.rankedMatch = doc.getBoolean(RANKED);
        replay.fogMode = doc.getInteger(FOG);
        replay.length = doc.getInteger(LENGTH);
        replay.activePlayers = doc.getInteger(PLAYER_COUNT);
        List<Document> playersDoc = (List<Document>) doc.get(PLAYERS);
        for (Document playerDoc : playersDoc) {
            ReplayPlayer replayPlayer = new ReplayPlayer();
            replayPlayer.name = playerDoc.getString(NAME);
            replayPlayer.result = playerDoc.getString(RESULT);
            replay.players.add(replayPlayer);
        }
        return replay;
    }



    private static Document toDocument(Object o) {
        Replay replay = (Replay) o;
        List<Document> playerArray = new ArrayList<>();
        for (ReplayPlayer p : replay.players) {
            playerArray.add(new Document()
                    .append(NAME, p.name.toLowerCase())
                    .append(RESULT, p.result)
                    .append(RANK, p.rank)
                    .append(KILLS, p.kills)
                    .append(DEATHS, p.deaths)
                    .append(ELIMINATIONS, p.eliminations)
                    .append(GOLD, p.gold)
                    .append(APM, p.apm)
                    .append(STAY_PERCENT, p.stayPercent));
        }
        return new Document()
                .append(MAP, replay.map)
                .append(VERSION, replay.version)
                .append(TURNS, replay.turns)
                .append(RANKED, replay.rankedMatch)
                .append(FOG, replay.fogMode)
                .append(LENGTH, replay.length)
                .append(GAME_TYPE, replay.gameType)
                .append(PLAYER_COUNT, replay.activePlayers)
                .append(GAME_ID, replay.gameId)
                .append(TIMESTAMP, new Date())
                .append(PLAYERS, playerArray);
    }

    private static Replay jsonToReplay(String json) throws IllegalStateException {
        Replay replay = new Replay();
        replay.gameId = -1;
        try {
            JsonParser parser = new JsonParser();
            JsonObject rootObj = parser.parse(json).getAsJsonObject();
            JsonObject bodyObj = rootObj.get("body").getAsJsonObject();
            replay.gameId = bodyObj.get("id").getAsInt();
            replay.length = bodyObj.get("length").getAsInt();
            JsonObject gameObj = bodyObj.get("data").getAsJsonObject().get("game").getAsJsonObject();
            replay.map = gameObj.get("map").getAsString();
            // TODO: Verify map or display error

            // Parse game settings
            JsonArray playersArray = gameObj.getAsJsonArray("players");
            String[] data = playersArray.get(0).getAsJsonObject()
                    .get("variables").getAsJsonObject()
                    .get("other").getAsString()
                .split(" ");
            // Check for if the game is ranked or not
            replay.rankedMatch = data[0].equals("1");
            if (!data[0].equals("0") && !data[0].equals("1"))
                throw new IllegalStateException("Unknown match status");
            // Fog mode
            replay.fogMode = Integer.parseInt(data[1]);
            if (!FogSettings.validate(replay.fogMode))
                throw new IllegalStateException("Invalid fog type");
            // Played turns
            replay.turns = Integer.parseInt(data[2]);
            // Map version verification
            replay.version = data[3];
            if (!ConfigManager.validVersions.contains(replay.version))
                throw new IllegalStateException("Invalid version found {" + replay.version + "}");

            // Parse player related data
            for (JsonElement element : playersArray) {
                JsonObject playerObj = element.getAsJsonObject();
                ReplayPlayer replayPlayer = new ReplayPlayer();
                replay.players.add(replayPlayer);

                // Default Data
                replayPlayer.name = playerObj.get("name").getAsString();
                replayPlayer.apm = playerObj.get("apm").getAsInt();
                replayPlayer.stayPercent = playerObj.get("stayPercent").getAsInt();

                // Custom Data
                JsonObject variablesObj = playerObj.get("variables").getAsJsonObject();
                replayPlayer.kills = variablesObj.get("kills").getAsInt();
                replayPlayer.deaths =  variablesObj.get("deaths").getAsInt();
                replayPlayer.team =  variablesObj.get("team").getAsInt();
                replayPlayer.eliminations =  variablesObj.get("eliminations").getAsInt();
                replayPlayer.gold = variablesObj.get("gold").getAsInt();
                replayPlayer.rank =  variablesObj.get("rank").getAsInt();
                replayPlayer.result = variablesObj.get("result").getAsString();
                if (!GameResult.validate(replayPlayer.result)) {
                    throw new IllegalStateException("Invalid result detected for {" + replayPlayer.name + "}");
                }
            }
            replay.update();
            replay.hash = replay.hashCode();
            return replay;
        } catch (Exception e) {
            if (replay.gameId == -1) {
                throw new IllegalStateException("Invalid game id");
            }
            if (e instanceof NullPointerException) {
                e.printStackTrace();
                throw new NullPointerException("Failed to parse replay data");
            }
            if (e instanceof IllegalStateException && (e.getMessage().equals("Not a JSON Object: null")
                    || e.getMessage().equals("Not a JSON Object: false"))) {
                throw new IllegalStateException("Failed to parse JSON properly");
            }
            throw e;
        }
    }


    public static boolean gameAlreadySaved(int gameId) {
        BasicDBObject query = new BasicDBObject();
        query.put(GAME_ID, gameId);
        MongoCursor<Document> cursor = col.find(query).iterator();
        try {
            return cursor.hasNext();
        } finally {
            cursor.close();
        }
    }

    public static void insert(Replay replay) {
        col.insertOne(toDocument(replay));
    }

    public static Replay fetchReplay(String url) throws Exception{
            String json = HttpHelper.fetchJsonData(url);
            return jsonToReplay(json);

    }

    public static List<Replay> findReplaysByPlayer(String name) {
        List<Replay> result = new ArrayList<>();
        BasicDBObject query = new BasicDBObject(PLAYERS + "." + NAME, name.toLowerCase());
        MongoCursor<Document> cursor = col.find(query).iterator();
        try {
            while(cursor.hasNext()) {
                Document replayDoc = cursor.next();
                Replay replay = toObject(replayDoc);
                if (replay.containsPlayer(name))
                    result.add(replay);
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    public static void setup(MongoCollection<Document> collection) {
        if (initialized)
            return;
        initialized = true;
        col = collection;
    }

}
