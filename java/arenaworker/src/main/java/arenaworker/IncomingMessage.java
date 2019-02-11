package arenaworker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;


public class IncomingMessage {
    static void Decode(String msg, Session session) {

        JSONObject json = new JSONObject(msg);
        String type = json.getString(("t"));

        try {
            Method method = IncomingMessage.class.getDeclaredMethod(type, JSONObject.class, Session.class);
            method.invoke(null, json, session);
        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
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

        String abilityType1 = json.optString("abilityType1");
        String abilityType2 = json.optString("abilityType2");
        String abilityType3 = json.optString("abilityType3");
        String abilityType4 = json.optString("abilityType4");

        if (abilityType1.length() == 0) abilityType1 = "Blasters";
        if (abilityType2.length() == 0) abilityType2 = "Blasters";
        if (abilityType3.length() == 0) abilityType3 = "Blasters";
        if (abilityType4.length() == 0) abilityType4 = "Blasters";

        if (game != null) {
            game.JoinGame(
                session,
                json.getString("name"),
                abilityType1,
                abilityType2,
                abilityType3,
                abilityType4
                );
        }
    }


    static void keyDown(JSONObject json, Session session) {
        Client client = Clients.GetClient(session);
        if (client != null) {
            client.player.KeyDown(json.getString("key"));
        }
    }

    static void keyUp(JSONObject json, Session session) {
        Client client = Clients.GetClient(session);
        if (client != null) {
            client.player.KeyUp(json.getString("key"));
        }
    }

    static void abilityKeyDown(JSONObject json, Session session) {
        Client client = Clients.GetClient(session);
        if (client != null) {
            client.player.AbilityKeyDown(json.getInt("num"));
        }
    }

    static void abilityKeyUp(JSONObject json, Session session) {
        Client client = Clients.GetClient(session);
        if (client != null) {
            client.player.AbilityKeyUp(json.getInt("num"));
        }
    }

    static void mousemove(JSONObject json, Session session) {
        Client client = Clients.GetClient(session);
        if (client != null) {
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