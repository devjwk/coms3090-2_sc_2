package onetoone.GroupMember;

import onetoone.Conversations.Conversation;
import onetoone.Conversations.ConvoRepository;
import onetoone.ConverstaionMembers.ConversationMember;
import onetoone.ConverstaionMembers.ConvoMemRepository;
import onetoone.Groups.Group;
import onetoone.Groups.GroupRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "GroupMembers", description = "Operations for managing group memberships")
public class GMController {

    @Autowired
    private GMRepository gmRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ConvoRepository convoRepository;

    @Autowired
    private ConvoMemRepository convoMemRepository;

    record GroupInfo(Long groupId, String groupName) {}
    record UserInfo(Long userId, String displayName, String groupName, Long groupId) {}

    // post - join group (req: group id, user id)
    // /gm/glist/{id}
    @PostMapping(path = "/gm/join")
    @Operation(summary = "Join group", description = "Adds a user to a group using User ID and Group ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    String joinGroup(@RequestBody Map<String, Object> body) {
        Long userId = ((Number) body.get("user_id")).longValue();
        Long groupId = ((Number) body.get("group_id")).longValue();
        Boolean mod = (Boolean) body.get("is_moderator");

        User user = userRepository.findById(userId).orElse(null);
        Group group = groupRepository.findById(groupId).orElse(null);

        if (user == null || group == null) {
            return "{\"message\":\"failure\"}";
        }

        GroupMember member = new GroupMember();

        member.setUserId(user);
        member.setGroupId(group);
        member.setStatus("active");
        member.setIs_moderator(mod);

        gmRepository.save(member);

        Optional<Conversation> convoOpt = convoRepository.findByGroupId(groupId);

        if (convoOpt.isPresent()) {
            Long conversationId = convoOpt.get().getConversationId();

            boolean exists = convoMemRepository
                    .existsByConversationIdAndUserId(conversationId, userId);

            if (!exists) {
                ConversationMember cm = new ConversationMember();
                cm.setConversationId(conversationId);
                cm.setUserId(userId);
                convoMemRepository.save(cm);
            }
        }

        return "{\"message\":\"success\"}";
    }

    // get - list group members (req: group id)
    // /gm/glist/{id}
    @GetMapping(path = "/gm/glist/{id}")
    @Operation(summary = "List group members", description = "Lists the members of a specific group using group ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group found"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    ResponseEntity<List<UserInfo>> listGroupMembers(@Parameter(description = "ID of the group to search through", required = true) @PathVariable Long id) {
        List<GroupMember> members = gmRepository.findByGroupId_groupId(id);

        if (members.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<UserInfo> users = members.stream()
                .map(luxray -> new UserInfo(
                        luxray.getUserId().getUserId(),
                        luxray.getUserId().getDisplayName(),
                        luxray.getGroupId().getGroupName(),
                        luxray.getGroupId().getGroupId()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    // get - list groups a user is in (req: user id)
    // /gm/ulist/{id}
    @GetMapping(path = "/gm/ulist/{id}")
    @Operation(summary = "List user groups", description = "Lists the groups a user is in using User ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    ResponseEntity<List<GroupInfo>> listUserGroups(@Parameter(description = "ID of the user to search through", required = true) @PathVariable Long id) {
        List<GroupMember> memberships = gmRepository.findByUserId_userId(id);

        if (memberships.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<GroupInfo> groups = memberships.stream()
                .map(hawkmon -> new GroupInfo(
                        hawkmon.getGroupId().getGroupId(),
                        hawkmon.getGroupId().getGroupName()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(groups);
    }

    // put - update membership status (req: membership id)
    // /gm/memstat/{id}
    @PutMapping(path = "/gm/memstat/{id}")
    @Operation(summary = "Update member status", description = "Updates a group member's status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Group member not found")
    })
    String updateMemStatus(@Parameter(description = "ID of the member to update", required = true) @PathVariable Long id, @RequestBody Map<String, String> body){
        Optional<GroupMember> gmOptional = gmRepository.findById(id);

        if (gmOptional.isEmpty()) {
            return "{\"message\":\"failure\"}";
        }

        String memStat = body.get("status");
        GroupMember member = gmOptional.get();

        member.setStatus(memStat);
        gmRepository.save(member);

        return "{\"message\":\"success\"}";
    }

    // put - update moderator status (req: membership id)
    // /gm/modstat/{id}
    @PutMapping(path = "/gm/modstat/{id}")
    @Operation(summary = "Update mod status", description = "Updates whether a member is a group moderator or not.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Group member not found")
    })
    String updateModStatus(@Parameter(description = "ID of the member to update", required = true) @PathVariable Long id, @RequestBody Map<String, Boolean> body){
        Optional<GroupMember> gmOptional = gmRepository.findById(id);

        if (gmOptional.isEmpty()) {
            return "{\"message\":\"failure\"}";
        }

        Boolean modStat = body.get("is_moderator");

        GroupMember member = gmOptional.get();

        member.setModStatus(modStat);
        gmRepository.save(member);

        return "{\"message\":\"success\"}";
    }

    // del - leave group (req: membership id)
    @DeleteMapping(path = "/gm/leave/{id}")
    @Operation(summary = "Leave group", description = "Removes a user from a group using membership ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member removed"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    String removeMember(@Parameter(description = "ID of the member to remove", required = true) @PathVariable Long id){
        gmRepository.deleteById(id);
        return "{\"message\":\"success\"}";
    }

    // Comment for merge rq
}