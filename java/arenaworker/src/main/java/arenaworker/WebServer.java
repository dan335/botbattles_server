package arenaworker;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class WebServer {

    static HttpServer server;

    public static void Run(int port) {

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/stats", new WebStatsHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}