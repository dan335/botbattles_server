package arenaworker;

import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;




public class SocketListener implements WebSocketListener {
    
    Session session;

    // rate limiting
    double rate = 400;   // num messages
    double per = 5000;  // per ms
    double allowance;
    long lastCheck;


    public static void SendJsonToSession(Session session, String json) {
        if (session.isOpen()) {
            try {
                Future<Void> future = session.getRemote().sendStringByFuture(json);
                future.get(2, TimeUnit.SECONDS);
            }
            catch (Throwable e) {
                
            }
            // catch (NullPointerException e) {
            //     // ignore
            // }
            // catch (TimeoutException e) {
            //     // ignore
            // }
            // catch (ExecutionException e) {
            //     // ignore
            // }
            // catch (Throwable e)
            // {
            //     //System.out.println("Error sending message.");
            //     //e.printStackTrace();
            // }
        }
    }
    
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        DisconnectSession();
        PartyManager.DestroyBySession(session);
    }

    @Override
    public void onWebSocketConnect(Session session)
    {
        this.session = session;
        this.session.setIdleTimeout(3600000L);   // 1 hour

        // start rate limit info
        allowance = rate;
        lastCheck = new Date().getTime();
    }

    @Override
    public void onWebSocketError(Throwable cause)
    {
        if (
            cause.getClass() != org.eclipse.jetty.io.EofException.class &&
            cause.getClass() != java.io.EOFException.class &&
            cause.getClass() != java.nio.channels.ClosedChannelException.class
        ) {
            //System.out.println("Error in onWebSocketError.");
            //cause.printStackTrace();
        }
    }

    @Override
    public void onWebSocketText(String message)
    {
        try {
            // rate limit
            long current = new Date().getTime();
            long timePassed = current - lastCheck;
            lastCheck = current;
            allowance += timePassed * (rate / per);
            if (allowance > rate) {
                allowance = rate;   // throttle
            }
            if (allowance < 1) {
                // limit
                System.out.println("Rate limiting session. " + session.getRemoteAddress().getAddress().getHostAddress());
                session.close();
                return;
            } else {
                allowance -= 1;
            }

            IncomingMessage.Decode(message, session);
        } catch (Throwable ex) {
            //System.err.println("Uncaught exception in SocketListener - " + ex.getMessage());
            //ex.printStackTrace(System.err);
        }
        
    }
    
    
    void DisconnectSession() {
        Client client = Clients.GetClient(this.session);

        if (client != null) {
            client.Destroy();
        }
    }
}
