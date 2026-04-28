package onetoone.Messages;

import onetoone.ConverstaionMembers.ConvoMemRepository;
import onetoone.Messages.dto.ChatMessageResponseDto;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class MessageController {

    @Autowired
    private MessagesRepository messagesRepository;

    @Autowired
    private ConvoMemRepository convoMemRepository;

    @Autowired
    private UserRepository usersRepository;

    @GetMapping("/messages/conversation/{conversationId}/user/{userId}")
    public ResponseEntity<List<ChatMessageResponseDto>> getMessagesForConversation(
            @PathVariable Long conversationId,
            @PathVariable Long userId
    ) {
        List<Messages> messages =
                messagesRepository.findByConversationIdOrderBySentAtAsc(conversationId);

        List<ChatMessageResponseDto> response = messages.stream()
                .map(message -> new ChatMessageResponseDto(
                        message.getMessageId(),
                        usersRepository.findById(message.getSenderUserId())
                                .map(user -> user.getDisplayName())
                                .orElse("Unknown"),
                        message.getRemoved() ? "Message removed by moderator" : message.getContent(),
                        message.getSentAt(),
                        message.getRemoved()
                ))
                .toList();

        return ResponseEntity.ok(response);
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
        message.setContent("Message removed by moderator");

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