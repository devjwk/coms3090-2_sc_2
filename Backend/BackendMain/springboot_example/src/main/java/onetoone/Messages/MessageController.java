package onetoone.Messages;

import onetoone.ConverstaionMembers.ConvoMemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @PutMapping("/moderators/{moderatorId}/messages/{messageId}/remove")
    public ResponseEntity<String> removeMessage(@PathVariable Long moderatorId,
                                                @PathVariable Long messageId) {

        Optional<Messages> msgOpt = messagesRepository.findById(messageId);
        if (msgOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Message not found");
        }

        Messages message = msgOpt.get();

        message.setRemoved(true);
        message.setRemovedBy(moderatorId);
        message.setRemovedReason("Removed by moderator");

        messagesRepository.save(message);

        return ResponseEntity.ok("Message removed");
    }

    @PutMapping("/moderators/{moderatorId}/messages/{messageId}/restore")
    public ResponseEntity<String> restoreMessage(@PathVariable Long moderatorId,
                                                 @PathVariable Long messageId) {

        Optional<Messages> msgOpt = messagesRepository.findById(messageId);
        if (msgOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Message not found");
        }

        Messages message = msgOpt.get();

        message.setRemoved(false);
        message.setRemovedBy(null);
        message.setRemovedReason(null);

        messagesRepository.save(message);

        return ResponseEntity.ok("Message restored");
    }
}