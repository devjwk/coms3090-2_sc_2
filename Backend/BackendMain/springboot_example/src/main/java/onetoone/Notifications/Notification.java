package onetoone.Notifications;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class Notification {
    private final SimpMessagingTemplate messagingTemplate;

    public Notification(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Send to a specific user
    public void sendNotification(Long userId, String message) {
        messagingTemplate.convertAndSend("/uver/notify/" + userId, message);
    }

    // Send to every user
    public void broadcast(String message) {
        messagingTemplate.convertAndSend("/uver/notify", message);
    }
}
