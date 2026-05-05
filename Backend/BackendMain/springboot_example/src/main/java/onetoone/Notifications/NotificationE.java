package onetoone.Notifications;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "notifications")
public class NotificationE {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public NotificationE() {}

    public NotificationE(Long userId, String type, String message, LocalDateTime timestamp) {
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
    }
}
