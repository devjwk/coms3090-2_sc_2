package onetoone.UserImages;

import onetoone.GroupMember.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByUserId_UserId(Long UserId);
    Image findByImageId(Long ImageId);
}
