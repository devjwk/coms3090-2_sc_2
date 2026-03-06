package onetoone.Matches;

import onetoone.Groups.Group;
import onetoone.Groups.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class MatchController {
    @Autowired
    MatchRepository MatchRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    /** Fetch full user profile by id (all fields from database). */
    @GetMapping(path = "/matches/user/{userId}")
    public List<Match> getMatchByUserId(@PathVariable Long userId) {
        return MatchRepository.findByUser1IdOrUser2Id(userId, userId);
    }

    @GetMapping(path = "/matches/{id}")
    ResponseEntity<Match> getMatchById(@PathVariable Long id) {
        Optional<Match> matchOptional = MatchRepository.findById(id);
        if (matchOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(matchOptional.get());
    }

    @PutMapping("/matches/edit/{id}")
    public ResponseEntity<Match> editGroup(@PathVariable Long id, @RequestBody Match req) {

        Optional<Match> matchOptional = MatchRepository.findById(id);

        if(matchOptional.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Match match = matchOptional.get();
        match.setStatus(req.getStatus());

        MatchRepository.save(match);

        return ResponseEntity.ok(match);
    }
}
