package onetoone.Moderators;

import onetoone.GroupMember.GMRepository;
import onetoone.GroupMember.GroupMember;
import onetoone.GroupMember.MembershipStatus;
import onetoone.Groups.Group;
import onetoone.Groups.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/moderators")
public class ModeratorController {

    @Autowired
    private ModeratorRepository moderatorRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GMRepository GMRepository;

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

    @GetMapping("/{moderatorId}/groups/{groupId}")
    public ResponseEntity<?> getOneManagedGroup(@PathVariable Long moderatorId,
                                                @PathVariable Long groupId) {

        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        return ResponseEntity.ok(groupOpt.get());
    }

    @GetMapping("/{moderatorId}/groups/{groupId}/members")
    public ResponseEntity<?> getApprovedMembers(@PathVariable Long moderatorId,
                                                @PathVariable Long groupId) {

        List<GroupMember> members =
                GMRepository.findByGroupId_GroupIdAndStatus(
                        groupId,
                        MembershipStatus.APPROVED
                );

        return ResponseEntity.ok(members);
    }

    @GetMapping("/{moderatorId}/groups/{groupId}/pending-members")
    public ResponseEntity<?> getPendingMembers(@PathVariable Long moderatorId,
                                               @PathVariable Long groupId) {

        List<GroupMember> pendingMembers =
                GMRepository.findByGroupId_GroupIdAndStatus(
                        groupId,
                        MembershipStatus.PENDING
                );

        return ResponseEntity.ok(pendingMembers);
    }

    @GetMapping("/{moderatorId}/groups/{groupId}/events")
    public ResponseEntity<?> getGroupEvents(@PathVariable Long moderatorId,
                                            @PathVariable Long groupId) {

        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        return ResponseEntity.ok(groupOpt.get().getEvents());
    }

    @GetMapping("/{moderatorId}/groups/{groupId}/announcements")
    public ResponseEntity<?> getGroupAnnouncements(@PathVariable Long moderatorId,
                                                   @PathVariable Long groupId) {

        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        return ResponseEntity.ok(groupOpt.get().getAnnouncements());
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






    /// -----------------------------------------------------------------------------------------------
    @PostMapping("/{moderatorId}/groups/{groupId}/events")
    public ResponseEntity<?> createGroupEvent(@PathVariable Long moderatorId,
                                              @PathVariable Long groupId,
                                              @RequestBody Map<String, String> body) {

        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOpt.get();

        if (!moderatorId.equals(group.getModeratorId())) {
            return ResponseEntity.status(403).body("You do not control this group");
        }

        String eventText = body.get("event");

        if (eventText == null || eventText.isBlank()) {
            return ResponseEntity.badRequest().body("Event text required");
        }

        String currentEvents = group.getEvents();

        if (currentEvents == null || currentEvents.isBlank()) {
            group.setEvents(eventText);
        } else {
            group.setEvents(currentEvents + "\n" + eventText);
        }

        groupRepository.save(group);

        return ResponseEntity.ok(group.getEvents());
    }

    @PostMapping("/{moderatorId}/groups/{groupId}/announcements")
    public ResponseEntity<?> createGroupAnnouncement(@PathVariable Long moderatorId,
                                                     @PathVariable Long groupId,
                                                     @RequestBody Map<String, String> body) {

        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOpt.get();

        if (!moderatorId.equals(group.getModeratorId())) {
            return ResponseEntity.status(403).body("You do not control this group");
        }

        String announcementText = body.get("announcement");

        if (announcementText == null || announcementText.isBlank()) {
            return ResponseEntity.badRequest().body("Announcement text required");
        }

        String currentAnnouncements = group.getAnnouncements();

        if (currentAnnouncements == null || currentAnnouncements.isBlank()) {
            group.setAnnouncements(announcementText);
        } else {
            group.setAnnouncements(currentAnnouncements + "\n" + announcementText);
        }

        groupRepository.save(group);

        return ResponseEntity.ok(group.getAnnouncements());
    }
}