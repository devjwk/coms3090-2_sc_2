package onetoone.GroupMember;

import onetoone.Conversations.Conversation;
import onetoone.Conversations.ConvoRepository;
import onetoone.ConverstaionMembers.ConversationMember;
import onetoone.ConverstaionMembers.ConvoMemRepository;
import onetoone.Groups.Group;
import onetoone.Groups.GroupRepository;
import onetoone.Notifications.Notification;
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

import static onetoone.GroupMember.MembershipStatus.APPROVED;

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
    private Notification notification;

    @Autowired
    private ConvoRepository convoRepository;

    @Autowired
    private ConvoMemRepository convoMemRepository;

    record GroupInfo(Long groupId, String groupName) {}
    record UserInfo(Long userId, String displayName, String groupName, Long groupId) {}

    // post - join group (req: group id, user id)
    // /gm/glist/{id}
    @PostMapping(path = "/gm/join")
    String joinGroup(@RequestBody Map<String, Object> body) {
        Long userId = ((Number) body.get("user_id")).longValue();
        Long groupId = ((Number) body.get("group_id")).longValue();

        User user = userRepository.findById(userId).orElse(null);
        Group group = groupRepository.findById(groupId).orElse(null);

        if (user == null || group == null) {
            return "{\"message\":\"failure\"}";
        }

        Optional<GroupMember> existing =
                gmRepository.findByGroupId_GroupIdAndUserId_UserId(groupId, userId);

        if (existing.isPresent()) {
            return "{\"message\":\"already_requested_or_member\"}";
        }

        GroupMember member = new GroupMember();
        member.setUserId(user);
        member.setGroupId(group);
        member.setStatus(MembershipStatus.PENDING);

        gmRepository.save(member);

        notification.sendNotification(userId,"GROUP_JOIN", "Your request to join " + group.getGroupName() + " was submitted.");

        return "{\"message\":\"success\"}";
    }
    // get - list group members (req: group id)
    // /gm/glist/{id}
    @GetMapping(path = "/gm/glist/{id}")
    ResponseEntity<List<UserInfo>> listGroupMembers(@PathVariable Long id) {
        List<GroupMember> members =
                gmRepository.findByGroupId_GroupIdAndStatus(id, APPROVED);

        List<UserInfo> users = members.stream()
                .map(member -> new UserInfo(
                        member.getUserId().getUserId(),
                        member.getUserId().getDisplayName(),
                        member.getGroupId().getGroupName(),
                        member.getGroupId().getGroupId()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    // get - list groups a user is in (req: user id)
    @GetMapping(path = "/gm/ulist/{id}")
    ResponseEntity<List<GroupInfo>> listUserGroups(@PathVariable Long id) {
        List<GroupMember> memberships =
                gmRepository.findByUserId_UserIdAndStatus(id, APPROVED);

        List<GroupInfo> groups = memberships.stream()
                .map(member -> new GroupInfo(
                        member.getGroupId().getGroupId(),
                        member.getGroupId().getGroupName()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(groups);
    }

    // put - update membership status (req: membership id)
    // /gm/memstat/{id}
    @PutMapping(path = "/gm/memstat/{id}")
    String updateMemStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<GroupMember> gmOptional = gmRepository.findById(id);

        if (gmOptional.isEmpty()) {
            return "{\"message\":\"failure\"}";
        }

        String statusText = body.get("status");

        GroupMember member = gmOptional.get();

        try {
            MembershipStatus status = MembershipStatus.valueOf(statusText.toUpperCase());
            member.setStatus(status);
            gmRepository.save(member);
        } catch (IllegalArgumentException e) {
            return "{\"message\":\"invalid_status\"}";
        }

        String groupName = member.getGroupId().getGroupName();
        if (member.getStatus() == APPROVED) {
            notification.sendNotification(member.getUserId().getUserId(), "GROUP_APPROVED", "Your membership to group " + groupName + " was approved!");
        }

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

        Optional<GroupMember> gmOptional = gmRepository.findById(id);
        if (gmOptional.isEmpty()) {
            return "{\"message\":\"failure\"}";
        }

        GroupMember leaving = gmOptional.get();
        String groupName = leaving.getGroupId().getGroupName();
        Long groupId = leaving.getGroupId().getGroupId();

        gmRepository.deleteById(id);

        List<GroupMember> remainingMembers = gmRepository.findByGroupId_groupId(groupId);
        for (GroupMember gm : remainingMembers) {
            notification.sendNotification(gm.getUserId().getUserId(), "GROUP_LEAVE", "A user has left " + groupName);
        }

        return "{\"message\":\"success\"}";
    }
}