package arenaworker;

import arenaworker.abilities.Ability;

public class PlayerInfo {

    public String id;
    public String name;
    public String userId;
    public boolean isWinner = false;
    public String[] abilities = new String[4];
    public int kills = 0;
    public double damageDealt = 0;

    public PlayerInfo(String id, String name, String userId, String[] abilities) {
        this.id = id;   // game object id
        this.name = name;
        this.userId = userId;
        this.abilities = abilities.clone();
    }
}