package onetoone.Messages;

import onetoone.ConverstaionMembers.ConvoMemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessagesRepository messagesRepository;

    @Autowired
    private ConvoMemRepository convoMemRepository;

    @GetMapping("/conversation/{conversationId}/user/{userId}")
    public List<Messages> getMessagesForConversation(@PathVariable Long conversationId,
                                                     @PathVariable Long userId) {

        return messagesRepository.findByConversationIdOrderBySentAtAsc(conversationId);
    }
}