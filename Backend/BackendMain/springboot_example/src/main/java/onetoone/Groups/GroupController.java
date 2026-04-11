package onetoone.Groups;

import onetoone.Conversations.Conversation;
import onetoone.Conversations.ConvoRepository;
import onetoone.ConverstaionMembers.ConversationMember;
import onetoone.ConverstaionMembers.ConvoMemRepository;
import onetoone.GroupMember.GMRepository;
import onetoone.GroupMember.GroupMember;
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

        List<GroupMember> memberships = groupMemberRepository.findByUserId_userId(userId);

        if (memberships.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<Group> groups = memberships.stream()
                .map(GroupMember::getGroupId) // returns Group
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

    @PostMapping(path = "/groups")
    @Operation(summary = "Create group", description = "Creates a new user group and its linked conversation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Group> createGroup(@RequestBody Group group) {
        if (group == null) {
            return ResponseEntity.badRequest().build();
        }

        Group savedGroup = groupRepository.save(group);

        Conversation conversation = new Conversation();
        conversation.setType("GROUP");
        conversation.setName(savedGroup.getGroupName());
        conversation.setGroupId(savedGroup.getGroupId());
        conversation.setCreatedAt(LocalDateTime.now());

        Conversation savedConversation = convoRepository.save(conversation);

        ConversationMember creator = new ConversationMember();
        creator.setConversationId(savedConversation.getConversationId());
        creator.setUserId(savedGroup.getCreatedBy());

        convoMemRepository.save(creator);

        return ResponseEntity.ok(savedGroup);
    }

    @PutMapping("/groups/edit/{id}")
    @Operation(summary = "Update group", description = "Updates group information fields such as name and description.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group updated successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    public ResponseEntity<Group> editGroup(
            @Parameter(description = "ID of the group to update", required = true)
            @PathVariable Long id,
            @RequestBody Group req) {

        Optional<Group> groupOptional = groupRepository.findById(id);

        if (groupOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Group group = groupOptional.get();
        group.setGroupName(req.getGroupName());
        group.setDescription(req.getDescription());

        Group savedGroup = groupRepository.save(group);

        Optional<Conversation> conversationOptional = convoRepository.findByGroupId(id);
        if (conversationOptional.isPresent()) {
            Conversation conversation = conversationOptional.get();
            conversation.setName(savedGroup.getGroupName());
            convoRepository.save(conversation);
        }

        return ResponseEntity.ok(savedGroup);
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
}