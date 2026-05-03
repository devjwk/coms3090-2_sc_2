package onetoone.Moderators;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onetoone.Announcements.Announcement;
import onetoone.Announcements.AnnouncementRepository;
import onetoone.Conversations.Conversation;
import onetoone.Conversations.ConvoRepository;
import onetoone.Events.GroupEvent;
import onetoone.Events.GroupEventRepository;
import onetoone.GroupMember.GMRepository;
import onetoone.GroupMember.GroupMember;
import onetoone.GroupMember.MembershipStatus;
import onetoone.Groups.Group;
import onetoone.Groups.GroupRepository;
import onetoone.Messages.MessagesRepository;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import onetoone.Messages.Messages;

import java.time.Instant;
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

    @Autowired
    private ConvoRepository convoRepository;

    @Autowired
    private MessagesRepository messagesRepository;

    @Autowired
    private GroupEventRepository groupEventRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

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

    @GetMapping("/{moderatorId}/groups/{groupId}/messages")
    public ResponseEntity<?> getGroupMessagesForModerator(
            @PathVariable Long moderatorId,
            @PathVariable Long groupId) {

        Optional<Moderator> modOpt = moderatorRepository.findById(moderatorId);
        if (modOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Moderator not found");
        }

        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOpt.get();

        if (group.getModeratorId() == null || !group.getModeratorId().equals(moderatorId)) {
            return ResponseEntity.status(403).body("Moderator is not assigned to this group");
        }

        Optional<Conversation> convoOpt = convoRepository.findByGroupId(groupId);
        if (convoOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Conversation not found for group");
        }

        Long conversationId = convoOpt.get().getConversationId();

        List<Messages> messages =
                messagesRepository.findByConversationIdOrderBySentAtAsc(conversationId);

        return ResponseEntity.ok(messages);
    }

    // ================= EVENTS =================

    @PostMapping("/{moderatorId}/groups/{groupId}/events")
    public ResponseEntity<?> createEvent(
            @PathVariable Long moderatorId,
            @PathVariable Long groupId,
            @RequestBody GroupEvent event
    ) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOpt.get();

        if (!moderatorId.equals(group.getModeratorId())) {
            return ResponseEntity.status(403).body("You do not control this group");
        }

        event.setGroupId(groupId);
        event.setModeratorId(moderatorId);

        return ResponseEntity.ok(groupEventRepository.save(event));
    }

    @GetMapping("/{moderatorId}/groups/{groupId}/events")
    public ResponseEntity<?> getEvents(
            @PathVariable Long moderatorId,
            @PathVariable Long groupId
    ) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOpt.get();

        if (!moderatorId.equals(group.getModeratorId())) {
            return ResponseEntity.status(403).body("You do not control this group");
        }

        return ResponseEntity.ok(groupEventRepository.findByGroupId(groupId));
    }

    @GetMapping("/{moderatorId}/groups/{groupId}/events/{eventId}")
    public ResponseEntity<?> getOneEvent(
            @PathVariable Long moderatorId,
            @PathVariable Long groupId,
            @PathVariable Long eventId
    ) {
        Optional<GroupEvent> eventOpt = groupEventRepository.findById(eventId);

        if (eventOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Event not found");
        }

        GroupEvent event = eventOpt.get();

        if (!event.getGroupId().equals(groupId)) {
            return ResponseEntity.status(403).body("Event does not belong to this group");
        }

        return ResponseEntity.ok(event);
    }

    @PutMapping("/{moderatorId}/groups/{groupId}/events/{eventId}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long moderatorId,
            @PathVariable Long groupId,
            @PathVariable Long eventId,
            @RequestBody GroupEvent updatedEvent
    ) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOpt.get();

        if (!moderatorId.equals(group.getModeratorId())) {
            return ResponseEntity.status(403).body("You do not control this group");
        }

        Optional<GroupEvent> eventOpt = groupEventRepository.findById(eventId);

        if (eventOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Event not found");
        }

        GroupEvent event = eventOpt.get();

        if (!event.getGroupId().equals(groupId)) {
            return ResponseEntity.status(403).body("Event does not belong to this group");
        }

        event.setTitle(updatedEvent.getTitle());
        event.setDescription(updatedEvent.getDescription());
        event.setLocation(updatedEvent.getLocation());
        event.setEventTime(updatedEvent.getEventTime());

        return ResponseEntity.ok(groupEventRepository.save(event));
    }

    @DeleteMapping("/{moderatorId}/groups/{groupId}/events/{eventId}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable Long moderatorId,
            @PathVariable Long groupId,
            @PathVariable Long eventId
    ) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOpt.get();

        if (!moderatorId.equals(group.getModeratorId())) {
            return ResponseEntity.status(403).body("You do not control this group");
        }

        Optional<GroupEvent> eventOpt = groupEventRepository.findById(eventId);

        if (eventOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Event not found");
        }

        GroupEvent event = eventOpt.get();

        if (!event.getGroupId().equals(groupId)) {
            return ResponseEntity.status(403).body("Event does not belong to this group");
        }

        groupEventRepository.delete(event);

        return ResponseEntity.ok("Event deleted");
    }


// ================= ANNOUNCEMENTS =================

    @PostMapping("/{moderatorId}/groups/{groupId}/announcements")
    public ResponseEntity<?> createAnnouncement(
            @PathVariable Long moderatorId,
            @PathVariable Long groupId,
            @RequestBody Announcement announcement
    ) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOpt.get();

        if (!moderatorId.equals(group.getModeratorId())) {
            return ResponseEntity.status(403).body("You do not control this group");
        }

        announcement.setGroupId(groupId);
        announcement.setModeratorId(moderatorId);
        announcement.setPinned(false);

        return ResponseEntity.ok(announcementRepository.save(announcement));
    }

    @GetMapping("/{moderatorId}/groups/{groupId}/announcements")
    public ResponseEntity<?> getAnnouncements(
            @PathVariable Long moderatorId,
            @PathVariable Long groupId
    ) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOpt.get();

        if (!moderatorId.equals(group.getModeratorId())) {
            return ResponseEntity.status(403).body("You do not control this group");
        }

        return ResponseEntity.ok(announcementRepository.findByGroupId(groupId));
    }

    @PutMapping("/{moderatorId}/groups/{groupId}/announcements/{announcementId}/pin")
    public ResponseEntity<?> pinAnnouncement(
            @PathVariable Long moderatorId,
            @PathVariable Long groupId,
            @PathVariable Long announcementId
    ) {
        Optional<Announcement> announcementOpt = announcementRepository.findById(announcementId);

        if (announcementOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Announcement not found");
        }

        Announcement announcement = announcementOpt.get();

        if (!announcement.getGroupId().equals(groupId)) {
            return ResponseEntity.status(403).body("Announcement does not belong to this group");
        }

        announcement.setPinned(true);

        return ResponseEntity.ok(announcementRepository.save(announcement));
    }

    @PutMapping("/{moderatorId}/groups/{groupId}/announcements/{announcementId}/unpin")
    public ResponseEntity<?> unpinAnnouncement(
            @PathVariable Long moderatorId,
            @PathVariable Long groupId,
            @PathVariable Long announcementId
    ) {
        Optional<Announcement> announcementOpt = announcementRepository.findById(announcementId);

        if (announcementOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Announcement not found");
        }

        Announcement announcement = announcementOpt.get();

        if (!announcement.getGroupId().equals(groupId)) {
            return ResponseEntity.status(403).body("Announcement does not belong to this group");
        }

        announcement.setPinned(false);

        return ResponseEntity.ok(announcementRepository.save(announcement));
    }

    @DeleteMapping("/{moderatorId}/groups/{groupId}/announcements/{announcementId}")
    public ResponseEntity<?> deleteAnnouncement(
            @PathVariable Long moderatorId,
            @PathVariable Long groupId,
            @PathVariable Long announcementId
    ) {
        Optional<Announcement> announcementOpt = announcementRepository.findById(announcementId);

        if (announcementOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Announcement not found");
        }

        Announcement announcement = announcementOpt.get();

        if (!announcement.getGroupId().equals(groupId)) {
            return ResponseEntity.status(403).body("Announcement does not belong to this group");
        }

        announcementRepository.delete(announcement);

        return ResponseEntity.ok("Announcement deleted");
    }

}