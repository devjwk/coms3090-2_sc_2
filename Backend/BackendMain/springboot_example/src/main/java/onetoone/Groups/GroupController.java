package onetoone.Groups;

import onetoone.Announcements.Announcement;
import onetoone.Announcements.AnnouncementRepository;
import onetoone.Conversations.Conversation;
import onetoone.Conversations.ConvoRepository;
import onetoone.ConverstaionMembers.ConversationMember;
import onetoone.ConverstaionMembers.ConvoMemRepository;
import onetoone.Events.GroupEvent;
import onetoone.Events.GroupEventRepository;
import onetoone.GroupMember.GMRepository;
import onetoone.GroupMember.GroupMember;
import onetoone.GroupMember.MembershipStatus;
import onetoone.Moderators.Moderator;
import onetoone.Moderators.ModeratorRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Groups", description = "Operations for managing user groups")
public class GroupController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    ConvoRepository convoRepository;

    @Autowired
    ConvoMemRepository convoMemRepository;

    @Autowired
    GMRepository groupMemberRepository;

    @Autowired
    ModeratorRepository moderatorRepository;

    @Autowired
    GroupEventRepository groupEventRepository;

    @Autowired
    AnnouncementRepository announcementRepository;

    private final String success = "{\"message\":\"success\"}";
    private final String failure = "{\"message\":\"failure\"}";

    private int countOverlap(List<String> userInterests, List<String> groupInterests) {
        if (userInterests == null || groupInterests == null) return 0;

        Set<String> userSet = new HashSet<>();
        for (String s : userInterests) {
            if (s != null) userSet.add(s.trim().toLowerCase());
        }

        Set<String> groupSet = new HashSet<>();
        for (String s : groupInterests) {
            if (s != null) groupSet.add(s.trim().toLowerCase());
        }

        userSet.retainAll(groupSet);
        return userSet.size();
    }

    @GetMapping("/groups/{groupId}/events")
    public ResponseEntity<?> getGroupEventsForUsers(@PathVariable Long groupId) {

        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        List<GroupEvent> events = groupEventRepository.findByGroupId(groupId);

        return ResponseEntity.ok(events);
    }

    @GetMapping("/groups/{groupId}/announcements")
    public ResponseEntity<?> getGroupAnnouncementsForUsers(@PathVariable Long groupId) {

        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        List<Announcement> announcements = announcementRepository.findByGroupId(groupId);

        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/groups/{groupId}/announcements/pinned")
    public ResponseEntity<?> getPinnedAnnouncements(@PathVariable Long groupId) {

        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        List<Announcement> pinned =
                announcementRepository.findByGroupIdAndPinnedTrue(groupId);

        return ResponseEntity.ok(pinned);
    }

    @GetMapping("/groups/{groupId}/announcements/unpinned")
    public ResponseEntity<?> getUnpinnedAnnouncements(@PathVariable Long groupId) {

        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        List<Announcement> unpinned =
                announcementRepository.findByGroupIdAndPinnedFalse(groupId);

        return ResponseEntity.ok(unpinned);
    }

    @GetMapping("/groups/recommend/{userId}")
    public List<Group> recommendGroups(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return new ArrayList<>();
        }

        User user = userOpt.get();
        List<Group> groups = groupRepository.findAll();

        groups.sort((g1, g2) -> Integer.compare(
                countOverlap(user.getInterests(), g2.getInterests()),
                countOverlap(user.getInterests(), g1.getInterests())
        ));

        return groups;
    }

    @GetMapping("/groups/search")
    public List<Group> searchGroups(@RequestParam(required = false) String keyword) {
        String cleanedKeyword = (keyword != null && !keyword.trim().isEmpty())
                ? keyword.trim()
                : null;

        return groupRepository.searchGroups(cleanedKeyword);
    }

    @GetMapping(path = "/groups/{id}")
    @Operation(summary = "Get group by ID", description = "Returns the group associated with the given group ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group found"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<Group> getGroupById(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable Long id) {

        Optional<Group> groupOptional = groupRepository.findById(id);
        if (groupOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groupOptional.get());
    }

    @GetMapping(path = "/groups")
    @Operation(summary = "Get all groups", description = "Returns a list of all user groups.")
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @GetMapping(path = "/groups/me/{userId}")
    public ResponseEntity<List<Group>> getMyGroups(@PathVariable Long userId) {

        List<GroupMember> memberships =
                groupMemberRepository.findByUserId_UserIdAndStatus(
                        userId,
                        MembershipStatus.APPROVED
                );

        List<Group> groups = memberships.stream()
                .map(GroupMember::getGroupId)
                .toList();

        return ResponseEntity.ok(groups);
    }

    @PostMapping("/groups/{groupId}/add/{userId}")
    public ResponseEntity<String> addUserToGroup(@PathVariable Long groupId,
                                                 @PathVariable Long userId) {

        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Optional<Conversation> convoOpt = convoRepository.findByGroupId(groupId);
        if (convoOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Conversation not found for group");
        }

        Long conversationId = convoOpt.get().getConversationId();

        boolean exists = convoMemRepository
                .existsByConversationIdAndUserId(conversationId, userId);

        if (exists) {
            return ResponseEntity.ok("User already in group");
        }

        // add user
        ConversationMember member = new ConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);

        convoMemRepository.save(member);

        return ResponseEntity.ok("User added to group");
    }

    @PostMapping("/moderators/{moderatorId}/groups")
    @Operation(summary = "Create group as moderator",
            description = "Only a valid moderator account can create a group. Moderator ID is auto-attached to the group.")
    public ResponseEntity<?> createGroupForModerator(@PathVariable Long moderatorId,
                                                     @RequestBody Group group) {

        if (group == null) {
            return ResponseEntity.badRequest().body("Invalid group body");
        }

        // verify moderator exists
        Optional<Moderator> moderatorOptional =
                moderatorRepository.findById(moderatorId);

        if (moderatorOptional.isEmpty()) {
            return ResponseEntity.status(403).body("Only moderators can create groups");
        }

        // auto assign moderator ownership
        group.setModeratorId(moderatorId);

        // save group
        Group savedGroup = groupRepository.save(group);

        // create linked conversation
        Conversation conversation = new Conversation();
        conversation.setType("GROUP");
        conversation.setName(savedGroup.getGroupName());
        conversation.setGroupId(savedGroup.getGroupId());
        conversation.setCreatedAt(LocalDateTime.now());

        Conversation savedConversation = convoRepository.save(conversation);

        return ResponseEntity.ok(savedGroup);
    }

    @PutMapping("/moderators/{moderatorId}/groups/{groupId}")
    public ResponseEntity<?> editGroup(@PathVariable Long moderatorId,
                                       @PathVariable Long groupId,
                                       @RequestBody Group req) {

        Optional<Moderator> modOpt = moderatorRepository.findById(moderatorId);
        if (modOpt.isEmpty()) {
            return ResponseEntity.status(403).body("Moderator not found");
        }

        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOpt.get();

        // verify ownership
        if (!moderatorId.equals(group.getModeratorId())) {
            return ResponseEntity.status(403).body("You do not control this group");
        }

        group.setGroupName(req.getGroupName());
        group.setDescription(req.getDescription());
        group.setInterests(req.getInterests());

        Group savedGroup = groupRepository.save(group);

        Optional<Conversation> convoOpt = convoRepository.findByGroupId(groupId);
        if (convoOpt.isPresent()) {
            Conversation convo = convoOpt.get();
            convo.setName(savedGroup.getGroupName());
            convoRepository.save(convo);
        }

        return ResponseEntity.ok(savedGroup);
    }

    @PutMapping("/moderators/{moderatorId}/groups/{groupId}/members/{userId}/approve")
    public ResponseEntity<String> approveMember(@PathVariable Long moderatorId,
                                                @PathVariable Long groupId,
                                                @PathVariable Long userId) {

        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOpt.get();

        if (!moderatorId.equals(group.getModeratorId())) {
            return ResponseEntity.status(403).body("You do not control this group");
        }

        Optional<GroupMember> memberOpt =
                groupMemberRepository.findByGroupId_GroupIdAndUserId_UserId(groupId, userId);

        if (memberOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Membership request not found");
        }

        GroupMember groupMember = memberOpt.get();
        groupMember.setStatus(MembershipStatus.APPROVED);
        groupMemberRepository.save(groupMember);

        Optional<Conversation> convoOpt = convoRepository.findByGroupId(groupId);
        if (convoOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Conversation not found");
        }

        Long conversationId = convoOpt.get().getConversationId();

        boolean alreadyInConversation =
                convoMemRepository.existsByConversationIdAndUserId(conversationId, userId);

        if (!alreadyInConversation) {
            ConversationMember member = new ConversationMember();
            member.setConversationId(conversationId);
            member.setUserId(userId);
            convoMemRepository.save(member);
        }

        return ResponseEntity.ok("Member approved");
    }

    @DeleteMapping("/groups/{id}")
    @Operation(summary = "Delete group", description = "Deletes a user group by ID and its linked conversation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group deleted"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<String> deleteGroup(
            @Parameter(description = "ID of the group to delete", required = true)
            @PathVariable Long id) {

        if (!groupRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Optional<Conversation> conversationOptional = convoRepository.findByGroupId(id);
        conversationOptional.ifPresent(convoRepository::delete);

        groupRepository.deleteById(id);
        return ResponseEntity.ok("Group deleted");
    }

    @DeleteMapping("/moderators/{moderatorId}/groups/{groupId}/members/{userId}")
    public ResponseEntity<String> removeMemberAsModerator(
            @PathVariable Long moderatorId,
            @PathVariable Long groupId,
            @PathVariable Long userId
    ) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOpt.get();

        if (!moderatorId.equals(group.getModeratorId())) {
            return ResponseEntity.status(403).body("You do not control this group");
        }

        Optional<GroupMember> memberOpt =
                groupMemberRepository.findByGroupId_GroupIdAndUserId_UserId(groupId, userId);

        if (memberOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not in group");
        }

        groupMemberRepository.delete(memberOpt.get());

        Optional<Conversation> convoOpt = convoRepository.findByGroupId(groupId);

        if (convoOpt.isPresent()) {
            Long conversationId = convoOpt.get().getConversationId();

            List<ConversationMember> members =
                    convoMemRepository.findByConversationId(conversationId);

            for (ConversationMember m : members) {
                if (m.getUserId().equals(userId)) {
                    convoMemRepository.delete(m);
                    break;
                }
            }
        }

        return ResponseEntity.ok("Member removed");
    }

    @DeleteMapping("/groups/{groupId}/leave/{userId}")
    public ResponseEntity<String> leaveGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId
    ) {
        Optional<GroupMember> memberOpt =
                groupMemberRepository.findByGroupId_GroupIdAndUserId_UserId(groupId, userId);

        if (memberOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not in group");
        }

        groupMemberRepository.delete(memberOpt.get());

        Optional<Conversation> convoOpt = convoRepository.findByGroupId(groupId);

        if (convoOpt.isPresent()) {
            Long conversationId = convoOpt.get().getConversationId();

            List<ConversationMember> members =
                    convoMemRepository.findByConversationId(conversationId);

            for (ConversationMember m : members) {
                if (m.getUserId().equals(userId)) {
                    convoMemRepository.delete(m);
                    break;
                }
            }
        }

        return ResponseEntity.ok("User left group");
    }

    @DeleteMapping("/groups/{groupId}/remove/{userId}")
    public ResponseEntity<String> removeUserFromGroup(@PathVariable Long groupId,
                                                      @PathVariable Long userId) {

        Optional<Conversation> convoOpt = convoRepository.findByGroupId(groupId);
        if (convoOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Conversation not found");
        }

        Long conversationId = convoOpt.get().getConversationId();

        List<ConversationMember> members =
                convoMemRepository.findByConversationId(conversationId);

        for (ConversationMember m : members) {
            if (m.getUserId().equals(userId)) {
                convoMemRepository.delete(m);
                return ResponseEntity.ok("User removed");
            }
        }

        return ResponseEntity.status(404).body("User not in group");
    }

    @DeleteMapping("/moderators/{moderatorId}/groups/{groupId}")
    @Operation(summary = "Delete group as moderator",
            description = "Deletes a group only if the moderator owns that group.")
    public ResponseEntity<String> deleteGroupAsModerator(@PathVariable Long moderatorId,
                                                         @PathVariable Long groupId) {

        if (!moderatorRepository.existsById(moderatorId)) {
            return ResponseEntity.status(403).body("Moderator not found");
        }

        Optional<Group> groupOptional = groupRepository.findById(groupId);

        if (groupOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOptional.get();

        if (!moderatorId.equals(group.getModeratorId())) {
            return ResponseEntity.status(403).body("You do not control this group");
        }

        Optional<Conversation> conversationOptional = convoRepository.findByGroupId(groupId);
        conversationOptional.ifPresent(convoRepository::delete);

        groupRepository.deleteById(groupId);

        return ResponseEntity.ok("Group deleted");
    }
}