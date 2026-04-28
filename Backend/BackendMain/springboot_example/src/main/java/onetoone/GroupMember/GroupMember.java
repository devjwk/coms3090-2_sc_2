package onetoone.GroupMember;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import onetoone.Groups.Group;
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
    private Long membershipId;

    //user_id FK
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User userId;

    //group_id FK
    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "groupId")
    private Group groupId;

    //joined_at timestamp
    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joined_at;

    @Column(nullable = false)
    private String status;

    // =============================== Getters and Setters for each field ================================== //

    public Long getMembershipId() { return membershipId; }
    public void setMembershipId(Long MembershipId) { this.membershipId = MembershipId; }

    public User getUserId() { return userId; }
    public void setUserId(User userId) { this.userId = userId; }

    public Group getGroupId() { return groupId; }
    public void setGroupId(Group groupId) { this.groupId = groupId; }

    public Instant getJoinedAt() { return joined_at; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}
