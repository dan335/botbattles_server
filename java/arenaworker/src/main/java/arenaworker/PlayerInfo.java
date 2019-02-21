package arenaworker;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.bson.types.ObjectId;


public class PlayerInfo {

    public String id;
    public String name;
    public String userId;
    public boolean isWinner = false;
    public String[] abilities;
    public int kills = 0;
    public double damageDealt = 0;
    public Document userData = null;
    public double ratingChange = 0;

    public PlayerInfo(String id, String name, String userId, String[] abilities) {
        this.id = id;   // game object id
        this.name = name;
        this.userId = userId;
        this.abilities = abilities.clone();

        if (userId != null) {
            MongoCollection<Document> collection = App.database.getCollection("users");
            userData = collection.find(eq("_id", new ObjectId(userId))).first();
        }
    }
}