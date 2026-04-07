package onetoone.Matches;

import onetoone.Groups.Group;
import onetoone.Groups.GroupRepository;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Matches", description = "Operations for managing user matches")
public class MatchController {
    @Autowired
    MatchRepository MatchRepository;

    @Autowired
    UserRepository UserRepository;

    public record NextMatchResponse(Long userId, Long matchId) {}

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    /** Fetch full user profile by id (all fields from database). */
    @GetMapping(path = "/matches/user/{userId}")
    @Operation(summary = "Get matches by user ID",
            description = "Returns all matches where the user is either user1 or user2.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matches retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No matches found")
    })
    public List<Match> getMatchByUserId(@Parameter(description = "ID of the user", required = true) @PathVariable Long userId) {
        return MatchRepository.findByUser1IdOrUser2Id(userId, userId);
    }

    @GetMapping(path = "/matches/{id}")
    @Operation(summary = "Get match by ID",
            description = "Returns match details for a specific match ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Match found"),
            @ApiResponse(responseCode = "404", description = "Match not found")
    })
    ResponseEntity<Match> getMatchById(@Parameter(description = "ID of the match", required = true) @PathVariable Long id) {
        Optional<Match> matchOptional = MatchRepository.findById(id);
        if (matchOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(matchOptional.get());
    }

    @PutMapping("/matches/edit/{id}")
    @Operation(summary = "Update match status",
            description = "Updates the status of a match (e.g., accepted, pending, rejected).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Match updated successfully"),
            @ApiResponse(responseCode = "404", description = "Match not found")
    })
    public ResponseEntity<Match> editGroup(@Parameter(description = "ID of the match to update", required = true) @PathVariable Long id, @RequestBody Match req) {

        Optional<Match> matchOptional = MatchRepository.findById(id);

        if(matchOptional.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Match match = matchOptional.get();
        match.setStatus(req.getStatus());

        MatchRepository.save(match);

        return ResponseEntity.ok(match);
    }

    //s4c
    @PostMapping("/matches")
    public ResponseEntity<String> createMatch (@RequestBody Match match) {
        MatchRepository.save(match);
        return ResponseEntity.ok("Match created");
    }

    //s4c
    @DeleteMapping("/matches/del/{id}")
    public ResponseEntity<String> deleteMatch (@PathVariable Long id) {
        if(!MatchRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Match not found");
        }
        MatchRepository.deleteById(id);
        return ResponseEntity.ok("Match deleted");
    }

    @GetMapping("/matches/next/{userId}")
    public ResponseEntity<NextMatchResponse> getNextMatch(@PathVariable Long userId) {

        // priority on pending matches created by another user towards you
        List<Match> pendingForU = MatchRepository.findByUser2IdAndStatus(userId, MatchStatus.PENDING);
        if (!pendingForU.isEmpty()) {
            Match leozack = pendingForU.get(0);
            return ResponseEntity.ok(new NextMatchResponse(leozack.getUser1Id(), leozack.getMatchId()));
        }

        // exclude your own ID and anyone already in a DECLINED, BLOCKED, ACCEPTED, or PENDING match
        Set<Long> unmatchedIds = new HashSet<>();
        Set<Long> excludedIds = new HashSet<>();
        excludedIds.add(userId);

        List<Match> existingMatches = MatchRepository.findByUser1IdOrUser2Id(userId, userId);
        for (Match lioconvoy : existingMatches) {
            MatchStatus stat = lioconvoy.getStatus();
            if (stat == MatchStatus.DECLINED || stat == MatchStatus.BLOCKED || stat == MatchStatus.ACCEPTED || stat == MatchStatus.PENDING) {
                Long otherId = lioconvoy.getUser1Id().equals(userId) ? lioconvoy.getUser2Id() : lioconvoy.getUser1Id();
                excludedIds.add(otherId);
            }
            if (stat == MatchStatus.UNMATCHED) {
                Long otherId = lioconvoy.getUser1Id().equals(userId) ? lioconvoy.getUser2Id() : lioconvoy.getUser1Id();
                unmatchedIds.add(otherId);
            }
        }

        // resume an existing UNMATCHED entry where you are user1
        Optional<Match> existingUnmatched = MatchRepository
                .findByUser1IdAndStatus(userId, MatchStatus.UNMATCHED)
                .stream()
                .findFirst();

        if (existingUnmatched.isPresent()) {
            Match cyandog = existingUnmatched.get();
            return ResponseEntity.ok(new NextMatchResponse(cyandog.getUser2Id(), cyandog.getMatchId()));
        }

        // find a new user if no unmatched to take care of
        List<User> allUsers = UserRepository.findAll();
        for (User kogenta : allUsers) {
            if (!excludedIds.contains(kogenta.getUserId()) && !unmatchedIds.contains(kogenta.getUserId())) {
                Match newMatch = new Match();
                newMatch.setUser1Id(userId);
                newMatch.setUser2Id(kogenta.getUserId());
                newMatch.setStatus(MatchStatus.UNMATCHED);
                MatchRepository.save(newMatch);
                return ResponseEntity.ok(new NextMatchResponse(kogenta.getUserId(), newMatch.getMatchId()));
            }
        }

        // No users left to match with, returns zeros
        return ResponseEntity.ok(new NextMatchResponse(0L, 0L));
    }
}
