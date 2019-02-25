package arenaworker;

import java.lang.reflect.Method;

import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONArray;
import org.json.JSONObject;


public class IncomingMessage {
    static void Decode(String msg, Session session) {

        JSONObject json = new JSONObject(msg);
        String type = json.getString(("t"));

        try {
            Method method = IncomingMessage.class.getDeclaredMethod(type, JSONObject.class, Session.class);
            method.invoke(null, json, session);
        } catch (Throwable ex) {
            System.out.println("json: " + json.toString());
            ex.printStackTrace();
        }
    }


    static void requestGame(JSONObject json, Session session) {
        Game game = GameManager.FindOrCreateGame();
        JSONObject obj = new JSONObject();
        obj.put("gameId", game.id);
        obj.put("t", "gameId");
        SendJsonToSession(session, obj.toString());
    }


    static void ping(JSONObject json, Session session) {
        JSONObject obj = new JSONObject();
        obj.put("t", "pong");
        SendJsonToSession(session, obj.toString());
    }


    // gameId
    static void joinGame(JSONObject json, Session session) {
        Game game = GameManager.GetGameById(json.getString("gameId"));

        if (game == null) return;

        String[] abilityTypes = new String[game.settings.numAbilities];

        JSONArray types = json.getJSONArray("abilityTypes");
        
        for (int i = 0; i < game.settings.numAbilities; i++) {
            abilityTypes[i] = (String)types.get(i);
        }

        String name = json.optString("name");
        String userId = json.optString("userId");

        if (name.length() == 0) name = "Noname";
        if (userId.length() == 0) userId = null;

        if (game != null) {
            game.JoinGame(
                session,
                name,
                userId,
                abilityTypes
                );
        }
    }


    static void keyDown(JSONObject json, Session session) {
        Client client = Clients.GetClient(session);
        if (client != null && client.player != null) {
            client.player.KeyDown(json.getString("key"));
        }
    }

    static void keyUp(JSONObject json, Session session) {
        Client client = Clients.GetClient(session);
        if (client != null && client.player != null) {
            client.player.KeyUp(json.getString("key"));
        }
    }

    static void abilityKeyDown(JSONObject json, Session session) {
        Client client = Clients.GetClient(session);
        if (client != null && client.player != null) {
            client.player.AbilityKeyDown(json.getInt("num"));
        }
    }

    static void abilityKeyUp(JSONObject json, Session session) {
        Client client = Clients.GetClient(session);
        if (client != null && client.player != null) {
            client.player.AbilityKeyUp(json.getInt("num"));
        }
    }

    static void mousemove(JSONObject json, Session session) {
        Client client = Clients.GetClient(session);
        if (client != null && client.player != null) {
            client.player.SetMousePosition(json.getDouble("x"), json.getDouble("y"));
        }
    }



    static void SendJsonToSession(Session session, String json) {
        if (session.isOpen()) {
            try {
                session.getRemote().sendStringByFuture(json);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
        }
    }
}