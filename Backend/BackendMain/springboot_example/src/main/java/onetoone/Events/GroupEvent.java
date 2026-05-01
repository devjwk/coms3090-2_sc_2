package onetoone.Events;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_events")
public class GroupEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private Long moderatorId;

    @Column(nullable = false)
    private String title;

    private String description;

    private String location;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    private LocalDateTime createdAt = LocalDateTime.now();

    public GroupEvent() {}

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getModeratorId() { return moderatorId; }
    public void setModeratorId(Long moderatorId) { this.moderatorId = moderatorId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}