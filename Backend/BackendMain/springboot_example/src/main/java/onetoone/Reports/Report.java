package onetoone.Reports;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onetoone.Matches.MatchStatus;
import onetoone.Users.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne
    @JoinColumn(name = "reporter", referencedColumnName = "userId")
    private User reporterId;

    @ManyToOne
    @JoinColumn(name = "reported", referencedColumnName = "userId")
    private User reportedId;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.IN_REVIEW;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
