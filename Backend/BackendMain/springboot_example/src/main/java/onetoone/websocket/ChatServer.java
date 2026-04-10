package onetoone.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import onetoone.ConverstaionMembers.ConversationMember;
import onetoone.ConverstaionMembers.ConvoMemRepository;
import onetoone.Messages.Messages;
import onetoone.Messages.MessagesRepository;
import onetoone.Messages.dto.ChatMessageDto;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ServerEndpoint("/chat/{userId}")
@Component
public class ChatServer {

    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // session -> userId
    private static final Map<Session, Long> sessionUserIdMap = new ConcurrentHashMap<>();

    // userId -> session
    private static final Map<Long, Session> userIdSessionMap = new ConcurrentHashMap<>();

    // userId -> displayName
    private static final Map<Long, String> userDisplayNameMap = new ConcurrentHashMap<>();

    private UserRepository getUserRepository() {
        return SpringContext.getBean(UserRepository.class);
    }

    private ConvoMemRepository getConvoMemRepository() {
        return SpringContext.getBean(ConvoMemRepository.class);
    }

    private MessagesRepository getMessagesRepository() {
        return SpringContext.getBean(MessagesRepository.class);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) throws IOException {
        logger.info("[onOpen] userId={}", userId);

        UserRepository userRepository = getUserRepository();
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            session.getBasicRemote().sendText("Invalid userId");
            session.close();
            return;
        }

        if (userIdSessionMap.containsKey(userId)) {
            session.getBasicRemote().sendText("User already connected");
            session.close();
            return;
        }

        User user = optionalUser.get();
        String displayName = user.getDisplayName();

        sessionUserIdMap.put(session, userId);
        userIdSessionMap.put(userId, session);
        userDisplayNameMap.put(userId, displayName);

        session.getBasicRemote().sendText("Welcome to the chat server, " + displayName);
    }

    @OnMessage
    public void onMessage(Session session, String jsonMessage) throws IOException {
        Long senderUserId = sessionUserIdMap.get(session);

        if (senderUserId == null) {
            session.getBasicRemote().sendText("Unknown session");
            return;
        }

        String senderDisplayName = userDisplayNameMap.getOrDefault(senderUserId, "User " + senderUserId);
        logger.info("[onMessage] {} ({}) : {}", senderDisplayName, senderUserId, jsonMessage);

        ChatMessageDto dto;
        try {
            dto = mapper.readValue(jsonMessage, ChatMessageDto.class);
        } catch (Exception e) {
            session.getBasicRemote().sendText("Invalid message format");
            return;
        }

        if (dto.getConversationId() == null || dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            session.getBasicRemote().sendText("conversationId and content are required");
            return;
        }

        // force sender to be the connected user
        dto.setSenderUserId(senderUserId);

        ConvoMemRepository convoMemRepository = getConvoMemRepository();
        MessagesRepository messagesRepository = getMessagesRepository();

        boolean allowed = convoMemRepository.existsByConversationIdAndUserId(
                dto.getConversationId(),
                senderUserId
        );

        if (!allowed) {
            session.getBasicRemote().sendText("You are not in this conversation");
            return;
        }

        Messages message = new Messages();
        message.setConversationId(dto.getConversationId());
        message.setSenderUserId(senderUserId);
        message.setContent(dto.getContent().trim());
        message.setSentAt(LocalDateTime.now());

        Messages savedMessage = messagesRepository.save(message);

        List<ConversationMember> members =
                convoMemRepository.findByConversationId(dto.getConversationId());

        String outgoing = mapper.writeValueAsString(savedMessage);

        for (ConversationMember member : members) {
            Long memberUserId = member.getUserId();
            Session targetSession = userIdSessionMap.get(memberUserId);

            if (targetSession != null && targetSession.isOpen()) {
                try {
                    targetSession.getBasicRemote().sendText(outgoing);
                } catch (IOException e) {
                    logger.info("[Send Exception] {}", e.getMessage());
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        Long userId = sessionUserIdMap.remove(session);

        if (userId != null) {
            String displayName = userDisplayNameMap.getOrDefault(userId, "User " + userId);

            userIdSessionMap.remove(userId);
            userDisplayNameMap.remove(userId);

            logger.info("[onClose] userId={} displayName={}", userId, displayName);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        Long userId = sessionUserIdMap.get(session);
        String displayName = (userId != null)
                ? userDisplayNameMap.getOrDefault(userId, "User " + userId)
                : "Unknown user";

        logger.error("[onError] {} : {}", displayName, throwable.getMessage(), throwable);
    }
}