package arenaworker;

import java.util.Calendar;
import java.util.Comparator;
import java.util.UUID;

import org.json.JSONObject;

class PartyChatMessage implements Comparable<PartyChatMessage> {

    public final String id = UUID.randomUUID().toString().substring(0, 8);
    PartyMember member;
    String text;
    long timestamp;

    public PartyChatMessage(PartyMember member, String text) {
        this.member = member;
        this.text = text;
        this.timestamp = Calendar.getInstance().getTimeInMillis();
    }


    public JSONObject GetJson() {
        JSONObject json = new JSONObject();
        json.put("name", member.name);
        json.put("date", (double)timestamp);
        json.put("text", text);
        json.put("id", id);
        return json;
    }


    @Override
    public int compareTo(PartyChatMessage o) {
        return Long.compare(timestamp, o.timestamp);
    }
}