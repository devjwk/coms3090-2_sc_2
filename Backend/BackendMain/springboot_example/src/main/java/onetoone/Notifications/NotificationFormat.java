package onetoone.Notifications;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationFormat {
    private String type;
    private String message;
    private LocalDateTime timestamp;

    public NotificationFormat(String type, String message) {
        this.type = type;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
