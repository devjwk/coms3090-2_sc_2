package onetoone.GroupMember;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GMRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByUser_id(String User_id);
    boolean existsByEmail(String email);
}