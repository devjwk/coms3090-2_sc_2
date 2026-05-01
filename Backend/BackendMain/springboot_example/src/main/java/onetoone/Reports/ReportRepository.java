package onetoone.Reports;

import onetoone.Groups.Group;
import onetoone.Matches.Match;
import onetoone.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReporterId_UserId(Long reporterId);
    List<Report> findByReportedId_UserId(Long reportedId);
    List<Report> findByStatus(ReportStatus status);
    long countByStatus(ReportStatus reportStatus);
}
