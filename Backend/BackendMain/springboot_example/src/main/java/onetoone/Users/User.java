package onetoone.Users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import onetoone.Matches.MatchStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_ts", nullable = false, updatable = false)
    private Instant createdTs;

    @Column(nullable = false)
    private Boolean active = true;

    private String displayName;

    @Column(length = 280)
    private String bio;

    @ElementCollection
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest")
    private List<String> interests;

    private String major;

    private Integer age;

    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.NEED_APPROVAL;

    // =============================== Getters and Setters for each field ================================== //

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Instant getCreatedTs() { return createdTs; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
}
