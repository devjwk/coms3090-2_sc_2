package onetoone.Conversations;

import onetoone.ConverstaionMembers.ConversationMember;
import onetoone.ConverstaionMembers.ConvoMemRepository;
import onetoone.Conversations.dto.DirectConversationRequest;
import onetoone.Conversations.dto.GroupConversationRequest;
import onetoone.Matches.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class ConversationController {

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    ConvoRepository convoRepository;

    @Autowired
    ConvoMemRepository convoMemRepository;

    @PostMapping("/conversations/direct")
    public Conversation createDirectConversation(@RequestBody DirectConversationRequest req) {
        Conversation conversation = new Conversation();
        conversation.setType("DIRECT");
        conversation.setName(null);
        conversation.setCreatedAt(LocalDateTime.now());

        Conversation savedConversation = convoRepository.save(conversation);

        ConversationMember member1 = new ConversationMember();
        member1.setConversationId(savedConversation.getConversationId());
        member1.setUserId(req.getUser1Id());

        ConversationMember member2 = new ConversationMember();
        member2.setConversationId(savedConversation.getConversationId());
        member2.setUserId(req.getUser2Id());

        convoMemRepository.save(member1);
        convoMemRepository.save(member2);

        return savedConversation;
    }

    @PostMapping("/conversations/group")
    public Conversation createGroupConversation(@RequestBody GroupConversationRequest req) {
        Conversation conversation = new Conversation();
        conversation.setType("GROUP");
        conversation.setName(req.getName());
        conversation.setCreatedAt(LocalDateTime.now());

        Conversation savedConversation = convoRepository.save(conversation);

        for (Long userId : req.getUserIds()) {
            ConversationMember member = new ConversationMember();
            member.setConversationId(savedConversation.getConversationId());
            member.setUserId(userId);
            convoMemRepository.save(member);
        }

        return savedConversation;
    }

    @GetMapping("/conversations/user/{userId}")
    public List<Conversation> getUserConversations(@PathVariable Long userId) {
        List<ConversationMember> memberships = convoMemRepository.findByUserId(userId);
        List<Conversation> conversations = new ArrayList<>();

        for (ConversationMember membership : memberships) {
            Optional<Conversation> convo = convoRepository.findById(membership.getConversationId());
            convo.ifPresent(conversations::add);
        }

        return conversations;
    }

    @GetMapping("/conversations/{conversationId}")
    public Conversation getConversation(@PathVariable Long conversationId) {
        return convoRepository.findById(conversationId).orElse(null);
    }

    @PostMapping("/conversations/{conversationId}/join/{userId}")
    public ConversationMember joinConversation(@PathVariable Long conversationId,
                                               @PathVariable Long userId) {

        // check if already in conversation
        boolean exists = convoMemRepository
                .existsByConversationIdAndUserId(conversationId, userId);

        if (exists) {
            throw new RuntimeException("User already in conversation");
        }

        if (!convoRepository.existsById(conversationId)) {
            throw new RuntimeException("Conversation does not exist");
        }

        ConversationMember member = new ConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);

        return convoMemRepository.save(member);
    }

    @DeleteMapping("/conversations/{conversationId}/leave/{userId}")
    public String leaveConversation(@PathVariable Long conversationId,
                                    @PathVariable Long userId) {

        List<ConversationMember> members =
                convoMemRepository.findByConversationId(conversationId);

        for (ConversationMember member : members) {
            if (member.getUserId().equals(userId)) {
                convoMemRepository.delete(member);
                return "User removed from conversation";
            }
        }

        return "User not found in conversation";
    }
}