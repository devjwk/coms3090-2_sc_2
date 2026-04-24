package onetoone.Moderators;

import onetoone.Groups.Group;
import onetoone.Groups.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/moderators")
public class ModeratorController {

    @Autowired
    private ModeratorRepository moderatorRepository;

    @Autowired
    private GroupRepository groupRepository;

    @PostMapping
    public ResponseEntity<Moderator> createModerator(@RequestBody Moderator moderator) {
        Moderator savedModerator = moderatorRepository.save(moderator);
        return ResponseEntity.ok(savedModerator);
    }

    @GetMapping
    public ResponseEntity<List<Moderator>> getAllModerators() {
        return ResponseEntity.ok(moderatorRepository.findAll());
    }

    @GetMapping("/{moderatorId}")
    public ResponseEntity<?> getModeratorById(@PathVariable Long moderatorId) {
        return moderatorRepository.findById(moderatorId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Moderator not found"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginModerator(@RequestBody Moderator loginRequest) {

        Optional<Moderator> moderatorOpt =
                moderatorRepository.findByEmail(loginRequest.getEmail());

        if (moderatorOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        Moderator moderator = moderatorOpt.get();

        if (!moderator.getPasswordHash().equals(loginRequest.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        if (!Boolean.TRUE.equals(moderator.getActive())) {
            return ResponseEntity.status(403).body("Moderator account inactive");
        }

        return ResponseEntity.ok(moderator);
    }

    @PutMapping("/{moderatorId}")
    public ResponseEntity<?> updateModerator(@PathVariable Long moderatorId,
                                             @RequestBody Moderator updatedModerator) {

        return moderatorRepository.findById(moderatorId)
                .<ResponseEntity<?>>map(existingModerator -> {

                    existingModerator.setDisplayName(updatedModerator.getDisplayName());
                    existingModerator.setEmail(updatedModerator.getEmail());
                    existingModerator.setPasswordHash(updatedModerator.getPasswordHash());

                    if (updatedModerator.getActive() != null) {
                        existingModerator.setActive(updatedModerator.getActive());
                    }

                    Moderator savedModerator = moderatorRepository.save(existingModerator);
                    return ResponseEntity.ok(savedModerator);
                })
                .orElseGet(() -> ResponseEntity.status(404).body("Moderator not found"));
    }

    @DeleteMapping("/{moderatorId}")
    public ResponseEntity<String> deleteModerator(@PathVariable Long moderatorId) {

        if (!moderatorRepository.existsById(moderatorId)) {
            return ResponseEntity.status(404).body("Moderator not found");
        }

        moderatorRepository.deleteById(moderatorId);
        return ResponseEntity.ok("Moderator deleted");
    }

    @GetMapping("/{moderatorId}/groups")
    public ResponseEntity<?> getGroupsForModerator(@PathVariable Long moderatorId) {

        if (!moderatorRepository.existsById(moderatorId)) {
            return ResponseEntity.status(404).body("Moderator not found");
        }

        List<Group> groups = groupRepository.findByModeratorId(moderatorId);
        return ResponseEntity.ok(groups);
    }
}