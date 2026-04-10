package onetoone.GroupMember;

import onetoone.UserImages.Image;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

public interface GMRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId_groupId(Long groupId);
    List<GroupMember> findByUserId_userId(Long userId);
    List<GroupMember> findByUserId(Long userId);
}