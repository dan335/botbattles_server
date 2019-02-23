package arenaworker;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

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
    CopyOnWriteArrayList<JSONArray> massMessages = new CopyOnWriteArrayList<>();
    JSONArray massMessage = new JSONArray();   // collect websocket messages into one message
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
        
        if (massMessages.size() > 0) {
            for (JSONArray json : massMessages) {
                JSONObject m = new JSONObject();
                m.put("t", "mass");
                m.put("m", json);

                if (session.isOpen()) {
                    try {
                        session.getRemote().sendStringByFuture(m.toString());
                        massMessage = new JSONArray();
                    }
                    catch (Throwable e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            massMessages.clear();
        }
    }


    public void SendJson(JSONArray json) {
        JSONArray group;
        
        if (massMessages.isEmpty()) {
            group = new JSONArray();
            massMessages.add(group);
        } else if (massMessages.get(massMessages.size()-1).length() >= maxMassMessageBatchSize) {
            group = new JSONArray();
            massMessages.add(group);
        } else {
            group = massMessages.get(massMessages.size()-1);
        }

        group.put(json);
    }


    public void SendJson(JSONObject json) {
        JSONArray group;
        
        if (massMessages.isEmpty()) {
            group = new JSONArray();
            massMessages.add(group);
        } else if (massMessages.get(massMessages.size()-1).length() >= maxMassMessageBatchSize) {
            group = new JSONArray();
            massMessages.add(group);
        } else {
            group = massMessages.get(massMessages.size()-1);
        }

        group.put(json);
    }

}