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
        Long user1 = req.getUser1Id();
        Long user2 = req.getUser2Id();

        if (user1.equals(user2)) {
            throw new RuntimeException("Cannot create conversation with yourself");
        }

        // find existing direct convo
        List<ConversationMember> user1Memberships = convoMemRepository.findByUserId(user1);

        for (ConversationMember membership : user1Memberships) {
            Long convoId = membership.getConversationId();

            Optional<Conversation> convoOpt = convoRepository.findById(convoId);
            if (convoOpt.isEmpty()) continue;

            Conversation convo = convoOpt.get();

            if (!"DIRECT".equals(convo.getType())) continue;

            List<ConversationMember> members = convoMemRepository.findByConversationId(convoId);

            if (members.size() == 2) {
                boolean hasUser1 = false;
                boolean hasUser2 = false;

                for (ConversationMember member : members) {
                    if (member.getUserId().equals(user1)) hasUser1 = true;
                    if (member.getUserId().equals(user2)) hasUser2 = true;
                }

                if (hasUser1 && hasUser2) {
                    return convo;
                }
            }
        }

        // create new if none exists
        Conversation conversation = new Conversation();
        conversation.setType("DIRECT");
        conversation.setName(null);
        conversation.setCreatedAt(LocalDateTime.now());

        Conversation savedConversation = convoRepository.save(conversation);

        ConversationMember m1 = new ConversationMember();
        m1.setConversationId(savedConversation.getConversationId());
        m1.setUserId(user1);

        ConversationMember m2 = new ConversationMember();
        m2.setConversationId(savedConversation.getConversationId());
        m2.setUserId(user2);

        convoMemRepository.save(m1);
        convoMemRepository.save(m2);

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