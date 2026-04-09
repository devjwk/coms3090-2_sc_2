package onetoone.Notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {
    @Autowired
    private Notification notification;

    @MessageMapping("/note/{id}")
    public void notif(@DestinationVariable Long id, String message) {
        notification.sendNotification(id, message);
    }
}
