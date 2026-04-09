package onetoone.Groups;

import onetoone.Matches.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByCategoryIgnoreCase(String category);

    @Query("""
        SELECT DISTINCT g
        FROM Group g
        LEFT JOIN g.interests i
        WHERE
            (:keyword IS NULL OR
             LOWER(g.groupName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(i) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND
            (:category IS NULL OR LOWER(g.category) = LOWER(:category))
    """)
    List<Group> searchGroups(@Param("keyword") String keyword);
}