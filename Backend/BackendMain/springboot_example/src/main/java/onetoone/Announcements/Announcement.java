package onetoone.Announcements;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long announcementId;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private Long moderatorId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    private boolean pinned = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Announcement() {}

    public Long getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(Long announcementId) { this.announcementId = announcementId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getModeratorId() { return moderatorId; }
    public void setModeratorId(Long moderatorId) { this.moderatorId = moderatorId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}