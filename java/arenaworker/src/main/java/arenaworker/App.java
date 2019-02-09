package arenaworker;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


public class App 
{

    public static MongoDatabase database;
    public static MongoClient mongoClient;

    public static void main( String[] args )
    {
        try {
            //local
            // mongoClient = new MongoClient("127.0.0.1", 3001);
            // database = mongoClient.getDatabase("meteor"); 

            Server server = new Server(3020);
            
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);
            
            // Add websocket servlet
            ServletHolder wsHolder = new ServletHolder("servlet", new SocketServlet());
            context.addServlet(wsHolder, "/ws");

            server.start();
            server.join();
            try {
                
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Arena worker started.");
        } catch (Throwable ex) {
            System.err.println("Uncaught exception - " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }
}
