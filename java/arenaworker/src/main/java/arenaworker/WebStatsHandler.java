package arenaworker;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONArray;
import org.json.JSONObject;


public class WebStatsHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject json = new JSONObject();
        JSONArray games = new JSONArray();

        int numPlayers = 0;
        int numClients = 0;

        for (Game game : GameManager.games.values()) {
            numPlayers += game.players.size();
            numClients += game.clients.size();

            JSONObject gameInfo = new JSONObject();
            gameInfo.put("id", game.id);
            gameInfo.put("isStarted", game.isStarted);
            gameInfo.put("isEnded", game.isEnded);
            gameInfo.put("numPlayers", game.players.size());
            gameInfo.put("numSpectators", game.clients.size() - game.players.size());
            games.put(gameInfo);
        }

        json.put("numPlayers", numPlayers);
        json.put("numSpectators", numClients - numPlayers);
        json.put("numGames", GameManager.games.size());
        json.put("games", games);

        String response = json.toString();

        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", "application/json");
        responseHeaders.set("Access-Control-Allow-Origin", "*");

        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}