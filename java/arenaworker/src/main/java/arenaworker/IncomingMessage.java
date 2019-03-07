package arenaworker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
        obj.put("time", Calendar.getInstance().getTimeInMillis());
        SendJsonToSession(session, obj.toString());
    }


    static void joinParty(JSONObject json, Session session) {
        String partyId = json.optString("partyId");
        String name = json.optString("name");
        if (partyId == "") return;
        if (name.length() == 0) name = "Noname";

        Party party = PartyManager.FindOrCreateParty(partyId);
        if (party != null) {
            party.JoinParty(new PartyMember(session, json.optString("id"), name));
        }
    }


    static void setReady(JSONObject json, Session session) {
        Party party = PartyManager.GetPartyById(json.optString("partyId"));
        if (party != null) {
            party.SetReady(session, json.getBoolean("isReady"));
        }
    }


    static void partyChatMessage(JSONObject json, Session session) {
        Party party = PartyManager.GetPartyById(json.optString("partyId"));
        if (party != null) {
            party.AddChat(session, json.optString("text"));
        }
    }


    // gameId
    static void joinGame(JSONObject json, Session session) {
        Game game = GameManager.GetGameById(json.getString("gameId"));

        if (game == null) return;

        ArrayList<String> abilityTypes = new ArrayList<>(game.settings.numAbilities);

        JSONArray types = json.getJSONArray("abilityTypes");
        
        for (int i = 0; i < game.settings.numAbilities; i++) {
            abilityTypes.add(types.optString(i));
            if (abilityTypes.get(i) == "") {
                abilityTypes.set(i, game.settings.defaultAbilityTypes[i]);
            }
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
                Future<Void> future = session.getRemote().sendStringByFuture(json);
                future.get(2, TimeUnit.SECONDS);
            }
            catch (Throwable e)
            {
                System.out.println("Error sending message to session.");
                e.printStackTrace();
            }
        }
    }
}