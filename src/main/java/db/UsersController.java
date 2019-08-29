package db;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import db.models.*;
import db.models.BNetUser;
import db.models.GameType;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static db.models.BNetUser.*;

public class UsersController {

    private static final String USER_NAME   = "uname";  // wc3 name
    private static final String DISC_NAME   = "dname";  // discord user name if any with discriminator
    private static final String APP_NAME    = "aname";  // Saves the user name in lower case
    private static final String WINS        = "w";
    private static final String LOSSES      = "l";
    private static final String KILLS       = "k";
    private static final String DEATHS      = "d";

    private static MongoCollection<Document> col;
    private static boolean initialized = false;

    private static final Users users = new Users();                             // Model containing all users
    private static final League soloLeague = new League(GameType.SOLO);
    private static final League teamLeague = new League(GameType.TEAM);
    private static final League ffaLeague = new League(GameType.FFA);

    private static final BNetUser toObject(Document doc) {
        BNetUser user = new BNetUser();
        user.name = doc.getString(USER_NAME);
        user.discordUser = doc.getString(DISC_NAME);
        for (String gameType : GameType.values) {
            Document statsDoc = (Document) doc.get(gameType);
            if (statsDoc != null) {
                Stats stats = user.stats(gameType);
                stats.wins = statsDoc.getInteger(WINS);
                stats.losses = statsDoc.getInteger(LOSSES);
                stats.deaths = statsDoc.getInteger(DEATHS);
                stats.kills = statsDoc.getInteger(KILLS);
            }
        }
        return user;
    }

    private static final Document toDocument(Object o) {
        BNetUser u = (BNetUser) o;
        Document userDoc = new Document()
                .append(USER_NAME, u.name)
                .append(APP_NAME, u.name.toLowerCase())
                .append(DISC_NAME, u.discordUser);
        for (String gameType : GameType.values) {
            Stats stats = u.stats(gameType);
            userDoc.append(gameType, new Document()
                    .append(WINS, stats.wins)
                    .append(LOSSES, stats.losses)
                    .append(KILLS, stats.kills)
                    .append(DEATHS, stats.deaths)
            );
        }
        return userDoc;
    }

    public static List<BNetUser> findUsersByMatchingName(String key) {
        return users.findManyByName(key);
    }

    /**
     * Queries first the in-memory cache and then the database for users by name.
     * @param name user name
     * @return BNetUser
     */
    public static BNetUser findByName(String name) {
        return users.findOneByName(name);
    }

    /**
     * Finds all users with the provided names by first querying the cache and then the database.
     * @param names list of names
     * @return list of users
     */
    public static List<BNetUser> findByNames(List<String> names) {
        List<BNetUser> foundUsers = new ArrayList<>();
        for (String name : names) {
            BNetUser user = users.findOneByName(name);
            if (user == null)
                throw new IllegalArgumentException("Could not find any user by the name {" + name + "}");
        }
        return foundUsers;
    }

    /**
     * Returns true if a user by the provided name exists.
     * @param name user name
     * @return boolean
     */
    public static boolean contains(String name) {
        return users.exists(name);
    }

    /**
     * Inserts a user into the cache and database if a user by that name does not already exist.
     * @throws IllegalArgumentException
     * @param user
     */
    public static void insert(BNetUser user) {
        users.insert(user);
        col.insertOne(toDocument(user));
    }

    /**
     * Removes a user from the cache and database
     * @param name username of entity to remove
     * @return true on success, false on failure
     */
    public static void delete(String name) {
        users.delete(findByName(name));
        BNetUser user = users.delete(findByName(name));
        if (user != null) {
            MongoCursor<Document> cursor = col.find(new Document(APP_NAME, name.toLowerCase())).iterator();
            while (cursor.hasNext())
                col.deleteOne(cursor.next());
            cursor.close();
        }
    }

    public static List<BNetUser> findAllRankedUsers(String gameType) {
        switch (gameType) {
            case GameType.SOLO: return soloLeague.getRankedUsers();
            case GameType.TEAM: return teamLeague.getRankedUsers();
            case GameType.FFA: return ffaLeague.getRankedUsers();
        }
        return new ArrayList<>();
    }

    /**
     * Updates a user entity
     * @param user object to update
     * @return true on success, false on failure
     */
    public static boolean update(BNetUser user) {
        if (user != null) {
            BasicDBObject query = new BasicDBObject(APP_NAME, user.name.toLowerCase());
            col.findOneAndReplace(query, toDocument(user));
            return true;
        }
        return false;
    }

    public static void updateStats(Replay replay) {
        List<BNetUser> replayUsers = new ArrayList<>();
        for (ReplayPlayer p : replay.players) {
            BNetUser u = users.findOneByName(p.name);
            if (u == null) {
                insert(u = new BNetUser(p.name));
            }
            replayUsers.add(u);
            // Update user stats
            BNetUser.Stats stats = u.stats(replay.gameType);
            stats.wins += (p.result.equals(GameResult.VICTORY))? 1 : 0;
            stats.losses += (p.result.equals(GameResult.DEFEAT))? 1 : 0;
            stats.kills += p.kills;
            stats.deaths += p.deaths;
            update(u);
        }
        List<BNetUser> changedUsers = null;
        switch (replay.gameType) {
            case GameType.SOLO: changedUsers = soloLeague.update(replayUsers); break;
            case GameType.TEAM: changedUsers = teamLeague.update(replayUsers); break;
            case GameType.FFA: changedUsers = ffaLeague.update(replayUsers); break;
        }
        for (BNetUser changedUser : changedUsers) {
            update(changedUser);
        }
    }

    /**
     * Injects the database collection of users and loads the cache up to a certain amount of users
     * @param collection database collection
     */
    public static void setup(MongoCollection<Document> collection) {
        if (initialized)
            return;
        initialized = true;
        col = collection;
        // Load users from database
        MongoCursor<Document> cursor = null;
        List<BNetUser> soloLeagueMembers = new ArrayList<>();
        List<BNetUser> teamLeagueMembers = new ArrayList<>();
        List<BNetUser> ffaLeagueMembers = new ArrayList<>();
        try {
            cursor = col.find().iterator();
            while (cursor.hasNext()) {
                BNetUser user = toObject(cursor.next());
                users.insert(user);
                if (user.stats(GameType.SOLO).wins > 0)
                    soloLeagueMembers.add(user);
                if (user.stats(GameType.TEAM).wins > 0)
                    teamLeagueMembers.add(user);
                if (user.stats(GameType.FFA).wins > 0)
                    ffaLeagueMembers.add(user);
            }
            // TODO: Temporary solution. Should store the leagues user names inside a collection
            soloLeague.update(soloLeagueMembers);
            teamLeague.update(teamLeagueMembers);
            ffaLeague.update(ffaLeagueMembers);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public static int count() {
        return users.count();
    }
}
