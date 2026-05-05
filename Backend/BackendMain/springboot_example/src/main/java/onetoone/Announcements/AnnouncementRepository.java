package onetoone.Announcements;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByGroupId(Long groupId);
    List<Announcement> findByGroupIdAndPinnedTrue(Long groupId);

    List<Announcement> findByGroupIdAndPinnedFalse(Long groupId);
}