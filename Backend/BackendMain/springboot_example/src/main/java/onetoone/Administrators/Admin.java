package onetoone.Administrators;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import onetoone.Users.User;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "admins")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User userId;

    @Column(name = "active", nullable = false)
    private Boolean isActiveAdmin = true;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;
}
