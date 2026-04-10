package onetoone.Groups;

import onetoone.GroupMember.GroupMember;
import onetoone.Matches.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    @Query("""
    SELECT DISTINCT g
    FROM Group g
    LEFT JOIN g.interests i
    WHERE
         (:keyword IS NULL OR
          LOWER(g.groupName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
          LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
          i LIKE CONCAT('%', :keyword, '%'))
    """)
    List<Group> searchGroups(@Param("keyword") String keyword);
}