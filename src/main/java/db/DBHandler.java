package db;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class DBHandler {

    private static final String HOST        = "192.168.99.100";
    private static final int PORT           = 32768;
    private static final String DB_NAME     = "db_wc3risk_test";        //     "db_wc3risk";

    public static MongoClient client = null;
    public static MongoDatabase db;

    public static void connect() {
        if (client == null) {
            client = new MongoClient(HOST, PORT);
            db = client.getDatabase(DB_NAME);

            // Setup Collections
            UsersController.setup(db.getCollection("users"));
            ReplaysController.setup(db.getCollection("replays"));
        }
    }

    public static void disconnect() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

}
