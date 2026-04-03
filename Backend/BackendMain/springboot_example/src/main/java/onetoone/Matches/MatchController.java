package onetoone.Matches;

import onetoone.Groups.Group;
import onetoone.Groups.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
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
}
