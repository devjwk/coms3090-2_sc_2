package onetoone.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/chat/{userId}")
@Component
public class ChatServer {

    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    // session -> userId
    private static final Map<Session, Long> sessionUserIdMap = new ConcurrentHashMap<>();

    // userId -> session
    private static final Map<Long, Session> userIdSessionMap = new ConcurrentHashMap<>();

    // userId -> displayName
    private static final Map<Long, String> userDisplayNameMap = new ConcurrentHashMap<>();

    private UserRepository getUserRepository() {
        return SpringContext.getBean(UserRepository.class);
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
        broadcast("[SYSTEM] " + displayName + " has joined the chat");
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        Long senderUserId = sessionUserIdMap.get(session);

        if (senderUserId == null) {
            session.getBasicRemote().sendText("Unknown session");
            return;
        }

        String senderDisplayName = userDisplayNameMap.get(senderUserId);

        logger.info("[onMessage] {} ({}) : {}", senderDisplayName, senderUserId, message);

        // DM format: @123 hello
        if (message.startsWith("@")) {
            String[] splitMsg = message.split("\\s+", 2);

            if (splitMsg.length < 2) {
                sendMessageToParticularUser(senderUserId, "[SYSTEM] Invalid DM format. Use: @userId message");
                return;
            }

            String targetPart = splitMsg[0].substring(1).trim();
            String actualMessage = splitMsg[1].trim();

            Long destUserId;
            try {
                destUserId = Long.parseLong(targetPart);
            } catch (NumberFormatException e) {
                sendMessageToParticularUser(senderUserId, "[SYSTEM] Invalid userId in DM");
                return;
            }

            if (!userIdSessionMap.containsKey(destUserId)) {
                sendMessageToParticularUser(senderUserId, "[SYSTEM] User " + destUserId + " is not connected");
                return;
            }

            String destDisplayName = userDisplayNameMap.getOrDefault(destUserId, "User " + destUserId);

            sendMessageToParticularUser(destUserId,
                    "[DM from " + senderDisplayName + "] " + actualMessage);

            sendMessageToParticularUser(senderUserId,
                    "[DM to " + destDisplayName + "] " + actualMessage);
        } else {
            broadcast(senderDisplayName + ": " + message);
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        Long userId = sessionUserIdMap.remove(session);

        if (userId != null) {
            String displayName = userDisplayNameMap.getOrDefault(userId, "User " + userId);

            userIdSessionMap.remove(userId);
            userDisplayNameMap.remove(userId);

            logger.info("[onClose] userId={} displayName={}", userId, displayName);
            broadcast("[SYSTEM] " + displayName + " disconnected");
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

    private void sendMessageToParticularUser(Long userId, String message) {
        Session targetSession = userIdSessionMap.get(userId);

        if (targetSession == null || !targetSession.isOpen()) {
            logger.info("[DM Exception] user {} not connected", userId);
            return;
        }

        try {
            targetSession.getBasicRemote().sendText(message);
        } catch (IOException e) {
            logger.info("[DM Exception] {}", e.getMessage());
        }
    }

    private void broadcast(String message) {
        sessionUserIdMap.forEach((session, userId) -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    logger.info("[Broadcast Exception] {}", e.getMessage());
                }
            }
        });
    }
}