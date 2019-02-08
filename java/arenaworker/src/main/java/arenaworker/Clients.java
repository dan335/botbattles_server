package arenaworker;

// so that we can find clients by session from SocketListener

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.websocket.api.Session;

public class Clients {
    static Map<Session, Client> clients = new ConcurrentHashMap<Session, Client>();


    public static void AddClient(Client client) {
        clients.put(client.session, client);
    }


    public static Client GetClient(Session session) {
        return clients.get(session);
    }
}