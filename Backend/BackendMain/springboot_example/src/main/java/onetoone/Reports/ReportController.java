package onetoone.Reports;

import io.swagger.v3.oas.annotations.Parameter;
import onetoone.Groups.Group;
import onetoone.Reports.Report;
import onetoone.Reports.ReportRepository;
import onetoone.Users.User;
import onetoone.Reports.ReportStatus;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static onetoone.Reports.ReportStatus.IN_REVIEW;

@RestController
public class ReportController {

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    //create report
    @PostMapping("/reports")
    public ResponseEntity<String> createReport (@RequestBody Report report) {
        User reporter = userRepository.findById(report.getReporterId().getUserId()).orElse(null);
        User reported = userRepository.findById(report.getReportedId().getUserId()).orElse(null);

        if (reporter == null) {
            return ResponseEntity.status(404).body("User reporter not found");
        }
        if (reported == null) {
            return ResponseEntity.status(404).body("User reported not found");
        }

        report.setReporterId(reporter);
        report.setReportedId(reported);
        report.setStatus(ReportStatus.IN_REVIEW);
        reportRepository.save(report);
        return ResponseEntity.ok("Report created");
    }

    //delete report
    @DeleteMapping("/reports/{id}")
    public ResponseEntity<String> delete (@PathVariable Long id) {
        if(!reportRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Report not found");
        }
        reportRepository.deleteById(id);
        return ResponseEntity.ok("Report deleted");
    }

    //edit report status
    @PutMapping("/reports/{id}")
    public ResponseEntity<String> editPerson(@PathVariable Long id, @RequestBody Report reportReq){
        //find the report being updated through ID
        Optional<Report> reportOptional = reportRepository.findById(id);

        //return a failure here if no report is found
        if (reportOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Report not found");
        }

        Report report = reportOptional.get();

        //status
        if (reportReq.getStatus() != null) {
            report.setStatus(reportReq.getStatus());
        }

        //save
        reportRepository.save(report);
        return ResponseEntity.ok("Report successfully edited");
    }

    //retrieve report
    @GetMapping("/reports/{id}")
    ResponseEntity<Report> getReportById(@PathVariable Long id) {
        Optional<Report> reportOptional = reportRepository.findById(id);
        if (reportOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reportOptional.get());
    }

    //retrieve all reports
    @GetMapping("/reports/all")
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    //retrieve all reports made by a certain reporter
    @GetMapping("/reports/reporter/{id}")
    public List<Report> getReportsOfReporter(@PathVariable Long id) {
        return reportRepository.findByReporterId_UserId(id);
    }

    //retrieve all reports made against a certain user
    @GetMapping("/reports/reported/{id}")
    public List<Report> getReportsOfReported(@PathVariable Long id) {
        return reportRepository.findByReportedId_UserId(id);
    }
}
