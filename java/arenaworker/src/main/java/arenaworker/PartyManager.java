package arenaworker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.websocket.api.Session;

public class PartyManager {
    static Map<String, Party> parties = new ConcurrentHashMap<String, Party>();

    public static void DestroyParty(Party party) {
        party.Destroy();
        parties.remove(party.id);
    }

    public static Party FindOrCreateParty(String id) {
        Party party = parties.get(id);

        if (party == null) {
            party = new Party(id);
            parties.put(party.id, party);
        }

        return party;
    }


    public static Party GetPartyById(String partyId) {
        return parties.get(partyId);
    }


    public static void DestroyBySession(Session session) {
        for (Party p : parties.values()) {
            p.LeaveParty(session);
        }
    }
}