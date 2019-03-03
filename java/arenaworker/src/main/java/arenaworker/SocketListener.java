package arenaworker;

import java.util.Date;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;




public class SocketListener implements WebSocketListener {
    
    Session session;

    // rate limiting
    double rate = 200;   // num messages
    double per = 5000;  // per ms
    double allowance;
    long lastCheck;

    
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        DisconnectSession();
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
        System.out.println(cause.getClass().getSimpleName() == "EofException");
        System.out.println(cause.getClass().getSimpleName());
        if (cause.getClass().getSimpleName() != "EofException") {
            System.out.println("Error in onWebSocketError.");
            cause.printStackTrace();
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
                System.out.println("Rate limiting session.");
                session.close();
                return;
            } else {
                allowance -= 1;
            }

            IncomingMessage.Decode(message, session);
        } catch (Throwable ex) {
            System.err.println("Uncaught exception in SocketListener - " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
        
    }
    
    
    void DisconnectSession() {
        Client client = Clients.GetClient(this.session);

        if (client != null) {
            client.Destroy();
        }
    }
}
