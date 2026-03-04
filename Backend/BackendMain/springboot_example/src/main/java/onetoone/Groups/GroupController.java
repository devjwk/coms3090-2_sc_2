package onetoone.Groups;

import onetoone.Groups.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@RestController
public class GroupController {

    @Autowired
    GroupRepository GroupRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    /** Fetch full user profile by id (all fields from database). */
    @GetMapping(path = "/groups/{id}")
    ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        Optional<Group> groupOptional = GroupRepository.findById(id);
        if (groupOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groupOptional.get());
    }

    @GetMapping(path = "/groups")
    public List<Group> getAllGroups() {
        return GroupRepository.findAll();
    }

    @PostMapping(path = "/groups")
    String createGroup(@RequestBody Group group) {
        if (group == null)
            return failure;
        GroupRepository.save(group);
        return success;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Group> editGroup(@PathVariable Long id, @RequestBody Group req) {

        Optional<Group> groupOptional = GroupRepository.findById(id);

        if(groupOptional.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Group group = groupOptional.get();
        group.setGroupName(req.getGroupName());
        group.setDescription(req.getDescription());

        GroupRepository.save(group);

        return ResponseEntity.ok(group);
    }

    // DELETE group
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGroup(@PathVariable Long id) {

        if(!GroupRepository.existsById(id)){
            return ResponseEntity.status(404).body("Group not found");
        }

        GroupRepository.deleteById(id);
        return ResponseEntity.ok("Group deleted");
    }

}