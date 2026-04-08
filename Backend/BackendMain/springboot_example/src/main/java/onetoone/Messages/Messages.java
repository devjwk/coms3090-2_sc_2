package onetoone.Messages;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import onetoone.Groups.Group;
import onetoone.Users.User;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Messages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    private Long conversationId;
    private Long senderUserId;

    @Column(nullable = false, length = 2000)
    private String content;

    private LocalDateTime sentAt;

    public Messages() {}
}
