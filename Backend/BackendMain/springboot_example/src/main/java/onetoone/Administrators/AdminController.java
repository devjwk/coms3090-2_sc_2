package onetoone.Administrators;

import onetoone.Administrators.AdminRepository;
import onetoone.Matches.Match;
import onetoone.Reports.Report;
import onetoone.Reports.ReportStatus;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
public class AdminController {
    @Autowired
    AdminRepository AdminRepository;

    @Autowired
    UserRepository UserRepository;

    //create
    @PostMapping("/admin")
    public ResponseEntity<String> createReport (@RequestBody Admin admin) {
        User adm = UserRepository.findById(admin.getUserId().getUserId()).orElse(null);

        if (adm == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        Admin r2 = AdminRepository.findByUserId_UserId(adm.getUserId());

        if (r2 != null) {
            return ResponseEntity.status(404).body("User already has an admin listing. Please update it instead.");
        }

        admin.setUserId(adm);
        admin.setIsActiveAdmin(true);
        AdminRepository.save(admin);
        return ResponseEntity.ok("Admin created");
    }

    //verify
    @GetMapping("/admin/{id}")
    public ResponseEntity<Boolean> checkAdmin (@PathVariable Long id) {
        Admin adm = AdminRepository.findByUserId_UserId(id);

        if (adm == null) {
            return ResponseEntity.status(404).body(false);
        }

        if (adm.getIsActiveAdmin() == true) {
            return ResponseEntity.status(200).body(true);
        }
        else {
            return ResponseEntity.status(200).body(false);
        }
    }

    //update isActiveAdmin (enable/disable admin powers)
    @PutMapping("/admin/{id}")
    public ResponseEntity<String> updateAdmin (@PathVariable Long id, @RequestBody Admin info) {
        Optional<Admin> matchOptional = AdminRepository.findById(id);

        if(matchOptional.isEmpty()){
            return ResponseEntity.status(404).body("Not found.");
        }

        Admin admin = matchOptional.get();
        admin.setIsActiveAdmin(info.getIsActiveAdmin());

        AdminRepository.save(admin);
        return ResponseEntity.ok("Admin updated");
    }

    //delete
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<String> deleteAdmin (@PathVariable Long id) {
        if(!AdminRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Admin not found");
        }
        AdminRepository.deleteById(id);
        return ResponseEntity.ok("Admin deleted");
    }
}
