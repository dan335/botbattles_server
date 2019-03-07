package arenaworker;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;


public class PartyMember {

    public Session session;
    public String id;
    public String name;
    public boolean isReady = false;

    public PartyMember(Session session, String id, String name) {
        this.session = session;
        this.id = id;
        this.name = name;
    }


    public void Destroy() {
        session.close();
    }


    public void SendJson(JSONObject json) {
        if (session.isOpen()) {
            try {
                Future<Void> future = session.getRemote().sendStringByFuture(json.toString());
                future.get(2, TimeUnit.SECONDS);
            }
            catch (Throwable e)
            {
                System.out.println("Error sending message to party member.");
                e.printStackTrace();
            }
        }
    }
}