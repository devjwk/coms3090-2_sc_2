package onetoone.Conversations;

import onetoone.Conversations.dto.DirectConversationRequest;
import onetoone.Conversations.dto.GroupConversationRequest;
import onetoone.Matches.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ConversationController {

    @Autowired
    MatchRepository MatchRepository;

    @PostMapping("/conversations/direct")
    public Conversation createDirectConversation(@RequestBody DirectConversationRequest req){

    }

    @PostMapping("/conversations/group")
    public Conversation createGroupConversation(@RequestBody GroupConversationRequest req);

    @GetMapping("/conversations/user/{userId}")
    public List<Conversation> getUserConversations(@PathVariable Long userId);

    @GetMapping("/conversations/{conversationId}")
    public Conversation getConversation(@PathVariable Long conversationId);
}
