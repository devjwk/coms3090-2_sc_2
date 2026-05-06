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

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class AdminController {
    @Autowired
    AdminRepository AdminRepository;

    @Autowired
    UserRepository UserRepository;

    //create
    @PostMapping("/admin/{id}")
    public ResponseEntity<String> createAdmin (@PathVariable Long id) {
        User adm = UserRepository.findById(id).orElse(null);

        if (adm == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        Admin r2 = AdminRepository.findByUserId_UserId(id);

        if (r2 != null) {
            return ResponseEntity.status(404).body("User already has an admin listing. Please update it instead.");
        }

        Admin admin = new Admin();
        admin.setUserId(adm);
        admin.setActiveAdmin(true);
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

        if (adm.getActiveAdmin() == true) {
            return ResponseEntity.status(200).body(true);
        }
        else {
            return ResponseEntity.status(200).body(false);
        }
    }

    @GetMapping("/admin/all")
    public List<Admin> listAdmins() {
        return AdminRepository.findAll();
    }

    //update isActiveAdmin (enable/disable admin powers)
    @PutMapping("/admin/{id}")
    public ResponseEntity<String> updateAdmin (@PathVariable Long id, @RequestBody Admin info) {
        Optional<Admin> matchOptional = AdminRepository.findById(id);

        if(matchOptional.isEmpty()){
            return ResponseEntity.status(404).body("Not found.");
        }

        Admin admin = matchOptional.get();

        if (info.getActiveAdmin() != null) {
            admin.setActiveAdmin(info.getActiveAdmin());
        }

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
