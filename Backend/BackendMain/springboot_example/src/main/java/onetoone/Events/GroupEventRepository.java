package onetoone.Events;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupEventRepository extends JpaRepository<GroupEvent, Long> {
    List<GroupEvent> findByGroupId(Long groupId);
}