package arenaworker;

import java.util.UUID;

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
    public String name = "Noname";
    JSONArray massMessage = new JSONArray();   // collect websocket messages into one message

    public Client(Session session, Game game, String name) {
        this.session = session;
        this.game = game;
        this.name = name;
    }


    public void AddPlayer(Player player) {
        this.player = player;
    }


    public void Destroy() {
        player.Destroy();
        game.clients.remove(this);
        Clients.clients.remove(session);

        if (game.clients.size() == 0) {
            GameManager.DestroyGame(game);
        }

        session.close();
    }


    public void Tick() {
        if (massMessage.length() > 0) {
            JSONObject m = new JSONObject();
            m.put("t", "mass");
            m.put("m", massMessage);
            
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
    }


    public void SendJson(JSONArray json) {
        massMessage.put(json);
    }


    public void SendJson(JSONObject json) {
        massMessage.put(json);
    }
}