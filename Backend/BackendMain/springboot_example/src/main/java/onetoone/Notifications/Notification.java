package onetoone.Notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Notification {
    @Autowired
    private NotificationHandler notificationHandler;

    // Send to a specific user
    public void sendNotification(Long userId, String type, String message) {
        NotificationFormat cargo = new NotificationFormat(type, message);
        notificationHandler.sendNotification(userId, cargo);
    }
}
