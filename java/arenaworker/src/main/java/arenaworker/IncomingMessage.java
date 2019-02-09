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
        if (game != null) {
            game.JoinGame(session);
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


    static void mousemove(JSONObject json, Session session) {
        Client client = Clients.GetClient(session);
        if (client != null) {
            client.player.mousePos.x = json.getDouble("x");
            client.player.mousePos.y = json.getDouble("y");
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