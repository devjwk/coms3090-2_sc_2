package onetoone.Notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
public class Notification {
    @Autowired
    private NotificationHandler notificationHandler;

    @Autowired
    private NotificationRepository notificationRepository;

    // Send to a specific user
    public void sendNotification(Long userId, String type, String message) {
        NotificationFormat cargo = new NotificationFormat(type, message);

        NotificationE entity = new NotificationE(userId, type, message, cargo.getTimestamp());
        notificationRepository.save(entity);

        notificationHandler.sendNotification(userId, cargo);
    }

    public List<NotificationE> getNotifications(Long userId) {
        return notificationRepository.findByUserId(userId);
    }
}
