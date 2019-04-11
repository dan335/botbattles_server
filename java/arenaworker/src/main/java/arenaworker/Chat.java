package arenaworker;

import static com.mongodb.client.model.Filters.eq;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Chat {

    ConcurrentHashMap<String, Set<Session>> rooms = new ConcurrentHashMap<>();

    public Chat() {

    }


    public void JoinChat(Session session, String roomId) {
        if (rooms.containsKey(roomId)) {
            Set<Session> sessions = rooms.get(roomId);
            if (sessions != null) {
                sessions.add(session);
            }
        } else {
            Set<Session> sessions = ConcurrentHashMap.newKeySet();
            sessions.add(session);
            rooms.put(roomId, sessions);
        }
    }


    public void LeaveChat(Session session, String roomId) {
        Set<Session> sessions = rooms.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
        }
    }


    public void LeaveAllChats(Session session) {
        for (Set<Session> sessions : rooms.values()) {
            sessions.remove(session);
        }
    }


    public static final MediaType JSONType = MediaType.get("application/json; charset=utf-8");
    OkHttpClient httpClient = new OkHttpClient();
    
    public void AddChatMessage(String roomId, String message, Client client, String usrId) {
        message = message.replaceAll("[^0-9a-zA-Z<>{}\"|;:.,~!?#$%^=&*\\]\\\\()\\[¿§«»ω⊙¤°℃℉€¥£¢¡®©0-9_+\\s]", "").trim();
        if (message == null || message.length() == 0) return;
        if (message.length() > 2000) return;

        JSONObject json = new JSONObject();
        json.put("t", "chat");
        json.put("msg", message);
        json.put("time", new Date().getTime());

        if (roomId.equals("frontpage")) {
            MongoCollection<Document> collection = App.database.getCollection("users");
            Document userData = collection.find(eq("_id", new ObjectId(usrId))).first();
            if (userData == null) return;
            json.put("name", userData.getString("username"));
        } else {
            json.put("name", client.name);
        }

        Set<Session> sessions = rooms.get(roomId);
        if (sessions != null) {
            for (Session s : sessions) {
                SocketListener.SendJsonToSession(s, json.toString());
            }
        }

        if (roomId.equals("frontpage")) {
            MongoCollection<Document> chatsCollection = App.database.getCollection("chats");

            Document doc = new Document("msg", message);
            doc.append("time", new Date());
            doc.append("userId", usrId);
            doc.append("name", json.optString("name"));

            chatsCollection.insertOne(doc);

            // send to discord
            if (App.isProduction) {
                JSONObject discordJson = new JSONObject();
                discordJson.put("username", "Chat Bot");
                discordJson.put("content", json.optString("name") + ": " + message);
    
                RequestBody body = RequestBody.create(JSONType, discordJson.toString());
                Request request = new Request.Builder()
                    .url("https://discordapp.com/api/webhooks/564536295381270539/HzsoBkey-itPA5_DGt3ov5OkfuGAkKQo-zPu6WPHCo08pdTN--S3vgj7nABEYvq-Mh2t")
                    .post(body)
                    .build();
    
                try {
                    Response res = httpClient.newCall(request).execute();
                    res.body().close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        }
    }

}

