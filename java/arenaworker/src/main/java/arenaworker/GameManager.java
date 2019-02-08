package arenaworker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    static Map<String, Game> games = new ConcurrentHashMap<String, Game>();

    public static void DestroyGame(Game game) {
        game.Destroy();
        games.remove(game.id);
    }

    public static Game FindOrCreateGame() {
        Game game = null;

        for (Game g : games.values()) {
            if (!g.isStarted) {
                game = g;
            }
        }

        if (game == null) {
            game = new Game(new Settings());
            games.put(game.id, game);
        }

        return game;
    }


    public static Game GetGameById(String gameId) {
        return games.get(gameId);
    }
}