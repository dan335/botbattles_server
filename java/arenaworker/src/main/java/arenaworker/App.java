package arenaworker;

import java.io.EOFException;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

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
            boolean isProduction = true;
            String ISPRODUCTION = System.getenv("ISPRODUCTION");
            
            if (ISPRODUCTION == null) {
                isProduction = false;
            } else {
                if (Boolean.parseBoolean(ISPRODUCTION) == false) {
                    isProduction = false;
                }
            }

            if (isProduction) {
                mongoClient = new MongoClient("arena-mongodb", 27017);
            } else {
                mongoClient = new MongoClient("127.0.0.1", 27017);
            }

            database = mongoClient.getDatabase("arena");

            // erase old replays
            MongoCollection<Document> replays = database.getCollection("replays");
            replays.drop();
            MongoCollection<Document> replaydata = database.getCollection("replaydatas");
            replaydata.drop();

            Server server = new Server(3020);
            
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);
            
            // Add websocket servlet
            ServletHolder wsHolder = new ServletHolder("servlet", new SocketServlet());
            context.addServlet(wsHolder, "/ws");

            WebServer.Run(3030);

            System.out.println("--- Bot Battles worker started ---");

            server.start();
            server.join();
        } catch (EOFException ex) {
            // do nothing
        } catch (Throwable ex) {
            System.err.println("Uncaught exception - " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }
}
