package onetoone.Notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationHandler extends TextWebSocketHandler {
    private final Map<Long, WebSocketSession> sessionTracker = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId != null) {
            sessionTracker.put(userId, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);
        if (userId != null) {
            sessionTracker.remove(userId);
        }
    }

    public void sendNotification(Long userId, NotificationFormat payload) {
        WebSocketSession session = sessionTracker.get(userId);
        if (session != null && session.isOpen()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                String json = mapper.writeValueAsString(payload);
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                sessionTracker.remove(userId);
            }
        }
    }

    private Long getUserId(WebSocketSession session) {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");

        try {
            return Long.parseLong(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
