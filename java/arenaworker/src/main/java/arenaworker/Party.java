package arenaworker;

import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONArray;
import org.json.JSONObject;

public class Party {

    public String id;
    public Set<PartyMember> members = ConcurrentHashMap.newKeySet();
    public ConcurrentSkipListSet<PartyChatMessage> chats = new ConcurrentSkipListSet<>();

    public Party(String id) {
        this.id = id;
    }


    public void JoinParty(PartyMember member) {
        if (members.size() < Settings.maxPlayers) {
            members.add(member);
            SendMemberList();
            SendChatTo(member);
        }
    }


    public void LeaveParty(Session session) {
        PartyMember member = null;

        for (PartyMember m : members) {
            if (m.session == session) {
                member = m;
            }
        }

        if (member != null) {
            members.remove(member);
            if (members.size() == 0) {
                PartyManager.DestroyParty(this);
            } else {
                SendMemberList();
            }
        }
    }


    public void AddChat(Session session, String message) {
        PartyMember member = null;

        for (PartyMember m : members) {
            if (m.session == session) {
                member = m;
            }
        }

        if (member != null) {
            chats.add(new PartyChatMessage(member, message));
            SendChat();
        }
    }


    void SendChat() {
        JSONObject json = ChatJson();

        for (PartyMember m : members) {
            m.SendJson(json);
        }
    }


    void SendChatTo(PartyMember member) {
        member.SendJson(ChatJson());
    }


    JSONObject ChatJson() {
        JSONObject json = new JSONObject();
        json.put("t", "partyChat");

        JSONArray chatArray = new JSONArray();

        for (PartyChatMessage chat : chats) {
            chatArray.put(chat.GetJson());
        }

        json.put("chat", chatArray);

        return json;
    }


    public void SetReady(Session session, boolean isReady) {
        PartyMember member = null;

        for (PartyMember m : members) {
            if (m.session == session) {
                member = m;
            }
        }

        if (member != null) {
            member.isReady = isReady;
            SendMemberList();
        }

        if (members.size() > 1) {
            boolean isAllReady = true;
            for (PartyMember m : members) {
                if (!m.isReady) {
                    isAllReady = false;
                }
            }

            if (isAllReady) {
                Game game = GameManager.FindOrCreateGame();
                JSONObject obj = new JSONObject();
                obj.put("gameId", game.id);
                obj.put("t", "gameId");

                for (PartyMember m : members) {
                    m.SendJson(obj);
                }
            }
        }
    }


    void SendMemberList() {
        JSONObject json = new JSONObject();
        json.put("t", "partyMembers");

        JSONArray list = new JSONArray();

        for (PartyMember m : members) {
            JSONObject member = new JSONObject();
            member.put("id", m.id);
            member.put("name", m.name);
            member.put("isReady", m.isReady);
            list.put(member);
        }

        json.put("list", list);

        for (PartyMember m : members) {
            m.SendJson(json);
        }
    }


    public void Destroy() {
        for (PartyMember m : members) {
            m.Destroy();
        }
        members.clear();
    }
}