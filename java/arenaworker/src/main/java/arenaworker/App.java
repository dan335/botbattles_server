package arenaworker;

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
            MongoCollection<Document> collection = database.getCollection("replays");
            collection.drop();

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
