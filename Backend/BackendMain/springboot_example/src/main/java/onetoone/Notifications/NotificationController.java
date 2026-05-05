package onetoone.Notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private Notification notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationE>> getNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long noteId) {
        if(!notificationRepository.existsById(noteId)) {
            return ResponseEntity.status(404).body("Notification not found");
        }
        notificationRepository.deleteById(noteId);
        return ResponseEntity.ok("Notification deleted");
    }
}
