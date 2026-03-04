package onetoone.GroupMember;

import onetoone.Users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
public class GMController {

    @Autowired
    private GMRepository gmRepository;

    // post - join group (req: group id, user id)
    @PostMapping(path = "/gm/join")
    String joinGroup(@RequestBody GroupMember member) {
        if (member == null) {
            return "{\"message\":\"failure\"}";
        }

        gmRepository.save(member);

        return "{\"message\":\"success\"}";
    }

    // get - list group members (req: group id)
    // /gm/glist/{id}

    // get - list groups a user is in (req: user id)
    // /gm/ulist/{id}

    // put - update membership status (req: membership id)
    // /gm/memstat/{id}
    @PutMapping(path = "/gm/memstat/{id}")
    String updateMemStatus(@PathVariable Long id, @RequestParam String memStat){
        Optional<GroupMember> gmOptional = gmRepository.findById(id);

        if (gmOptional.isEmpty()) {
            return "{\"message\":\"failure\"}";
        }

        GroupMember member = gmOptional.get();

        member.setStatus(memStat);

        return "{\"message\":\"success\"}";
    }

    // put - update moderator status (req: membership id)
    // /gm/modstat/{id}
    @PutMapping(path = "/gm/modstat/{id}")
    String updateModStatus(@PathVariable Long id, @RequestParam Boolean modStat){
        Optional<GroupMember> gmOptional = gmRepository.findById(id);

        if (gmOptional.isEmpty()) {
            return "{\"message\":\"failure\"}";
        }

        GroupMember member = gmOptional.get();

        member.setModStatus(modStat);

        return "{\"message\":\"success\"}";
    }

    // del - leave group (req: membership id)
    @DeleteMapping(path = "/gm/leave/{id}")
    String removeMember(@PathVariable Long id){
        gmRepository.deleteById(id);
        return "{\"message\":\"success\"}";
    }
}