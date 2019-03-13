package arenaworker;

import org.eclipse.jetty.websocket.api.Session;


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
}