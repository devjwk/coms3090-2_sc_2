package onetoone.Groups;

import onetoone.Groups.Group;
import onetoone.Notifications.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@Tag(name = "Groups", description = "Operations for managing user groups")
public class GroupController {

    @Autowired
    GroupRepository GroupRepository;

    @Autowired
    Notification notification;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    /** Fetch full user profile by id (all fields from database). */
    @GetMapping(path = "/groups/{id}")
    @Operation(summary = "Get group by ID", description = "Returns the group associated with the given group ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group found"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    ResponseEntity<Group> getGroupById(@Parameter(description = "ID of the group", required = true) @PathVariable Long id) {
        Optional<Group> groupOptional = GroupRepository.findById(id);
        if (groupOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groupOptional.get());
    }

    @GetMapping(path = "/groups")
    @Operation(summary = "Get all groups", description = "Returns a list of all user groups.")
    public List<Group> getAllGroups() {
        notification.sendNotification(1L, "Testing");
        return GroupRepository.findAll();
    }

    @PostMapping(path = "/groups")
    @Operation(summary = "Create group", description = "Creates a new user group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    String createGroup(@RequestBody Group group) {
        if (group == null)
            return failure;
        GroupRepository.save(group);
        return success;
    }

    @PutMapping("/groups/edit/{id}")
    @Operation(summary = "Update group", description = "Updates group information fields such as name and description.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group updated successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<Group> editGroup(@Parameter(description = "ID of the group to update", required = true) @PathVariable Long id, @RequestBody Group req) {

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
    @DeleteMapping("/groups/{id}")
    @Operation(summary = "Delete group", description = "Deletes a user group by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group deleted"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<String> deleteGroup(@Parameter(description = "ID of the group to delete", required = true) @PathVariable Long id) {
        if(!GroupRepository.existsById(id)){
            return ResponseEntity.status(404).body("Group not found");
        }

        GroupRepository.deleteById(id);
        return ResponseEntity.ok("Group deleted");
    }

}