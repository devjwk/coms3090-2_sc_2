package onetoone.GroupMember;

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


@RestController
public class GMController {

    @Autowired
    private GMRepository gmRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    record GroupInfo(Long groupId, String groupName) {}
    record UserInfo(Long userId, String displayName) {}

    // post - join group (req: group id, user id)
    // /gm/glist/{id}
    @PostMapping(path = "/gm/join")
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

        return "{\"message\":\"success\"}";
    }

    // get - list group members (req: group id)
    // /gm/glist/{id}
    @GetMapping(path = "/gm/glist/{id}")
    ResponseEntity<List<UserInfo>> listGroupMembers(@PathVariable Long id) {
        List<GroupMember> members = gmRepository.findByGroupId_groupId(id);

        if (members.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<UserInfo> users = members.stream()
                .map(luxray -> new UserInfo(
                        luxray.getUserId().getUserId(),
                        luxray.getUserId().getDisplayName()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    // get - list groups a user is in (req: user id)
    // /gm/ulist/{id}
    @GetMapping(path = "/gm/ulist/{id}")
    ResponseEntity<List<GroupInfo>> listUserGroups(@PathVariable Long id) {
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
    String updateMemStatus(@PathVariable Long id, @RequestBody Map<String, String> body){
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
    String updateModStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body){
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
    String removeMember(@PathVariable Long id){
        gmRepository.deleteById(id);
        return "{\"message\":\"success\"}";
    }
}