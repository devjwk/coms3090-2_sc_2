package onetoone.GroupMember;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import onetoone.Users.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "groupmembers")
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_id")
    private Long membership_id;

    //user_id FK
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user_id;

    //group_id FK
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id", referencedColumnName = "group_id")
    private Group group_id;

    //joined_at timestamp
    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joined_at;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Boolean is_moderator;

    // =============================== Getters and Setters for each field ================================== //

    public Long getMembershipId() { return membership_id; }
    public void setMembershipId(Long MembershipId) { MembershipId = membership_id; }

    public User getUserId() { return user_id; }
    public void setUserId(User userId) { userId = user_id; }

    //get/set group id

    public Instant getJoinedAt() { return joined_at; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getModStatus() { return is_moderator; }
    public void setModStatus(Boolean modStatus) { modStatus = is_moderator; }
}
