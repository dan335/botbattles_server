package arenaworker;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;


@SuppressWarnings("serial")
@WebSocket(maxIdleTime=3600000)
public class SocketServlet extends WebSocketServlet {
    
    @Override
    public void configure(WebSocketServletFactory factory)
    {
        factory.getPolicy().setIdleTimeout(3600000L);
        factory.register(SocketListener.class);
    } 
}
