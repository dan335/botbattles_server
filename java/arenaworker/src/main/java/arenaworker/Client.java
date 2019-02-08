package arenaworker;

// client contains a session
// one per session
// used to keep track of sessions

import org.eclipse.jetty.websocket.api.Session;

public class Client {
    Session session;
    Game game;
    Player player;

    public Client(Session session, Game game) {
        this.session = session;
        this.game = game;
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
    }


    public void SendJson(String json) {
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