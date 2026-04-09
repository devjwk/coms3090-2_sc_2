package onetoone.Conversations.dto;

import java.util.List;

public class GroupConversationRequest {
    private String name;
    private List<Long> userIds;

    public GroupConversationRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Long> getUserIds() { return userIds; }
    public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
}
