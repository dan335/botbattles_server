package arenaworker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

// client contains a session
// one per session
// used to keep track of sessions

import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONArray;
import org.json.JSONObject;

public class Client {
    public final String id = UUID.randomUUID().toString().substring(0, 8);
    final int hashcode = id.hashCode();
    Session session;
    Game game;
    Player player;
    String userId;
    public String name = "Noname";
    ConcurrentLinkedQueue<JSONObject> massMessages = new ConcurrentLinkedQueue<JSONObject>();
    int maxMassMessageBatchSize = 5;

    public Client(Session session, Game game, String name, String userId) {
        this.session = session;
        this.game = game;
        this.name = name;
        this.userId = userId;
        Clients.AddClient(this);
    }


    public void AddPlayer(Player player) {
        this.player = player;
    }


    public void Destroy() {
        if (player != null) {
            player.Destroy();
        }

        JSONObject json = new JSONObject();
        json.put("t", "clientDisconnected");
        json.put("name", name);
        game.SendJsonToClients(json);
        
        game.clients.remove(this);
        Clients.clients.remove(session);

        if (game.clients.size() == 0) {
            GameManager.DestroyGame(game);
        }

        session.close();
    }


    public void Tick() {
        List<JSONArray> groups = new ArrayList<>();
        JSONArray temp = new JSONArray();

        for (JSONObject obj = massMessages.poll(); obj != null; obj = massMessages.poll()) {
            temp.put(obj);

            if (temp.length() >= maxMassMessageBatchSize) {
                groups.add(temp);
                temp = new JSONArray();
            }
        }

        if (!temp.isEmpty()) {
            groups.add(temp);
        }

        for (JSONArray group : groups) {
            JSONObject m = new JSONObject();
            m.put("t", "mass");
            m.put("m", group);

            SocketListener.SendJsonToSession(session, m.toString());
        }
    }


    public void SendJson(JSONObject json) {
        massMessages.add(json);
    }

}