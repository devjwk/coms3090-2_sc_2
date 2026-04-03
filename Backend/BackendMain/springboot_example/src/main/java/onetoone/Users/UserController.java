package onetoone.Users;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@Tag(name = "Users", description = "Operations for managing users")
public class UserController {

    @Autowired
    UserRepository UserRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    /** Fetch full user profile by id (all fields from database). */
    @GetMapping(path = "/users/{id}")
    @Operation(summary = "Get user by ID", description = "Returns the user associated with the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    ResponseEntity<User> getUserById(@Parameter(description = "ID of the user", required = true) @PathVariable Long id) {
        Optional<User> userOptional = UserRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userOptional.get());
    }


    @PostMapping(path = "/users")
    @Operation(summary = "Create user", description = "Creates a new user account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    String createUser(@Parameter(description = "Information for a new user", required = true)@RequestBody User User){
        if (User == null)
            return failure;
        UserRepository.save(User);
        return success;
    }

    @DeleteMapping(path = "/users/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user account by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    String deletePerson(@Parameter(description = "ID of the user to delete", required = true) @PathVariable Long id){
        UserRepository.deleteById(id);
        return success;
    }

    //s4c
    //Edit profile
    @PutMapping(path = "/users/edit/{id}")
    @Operation(summary = "Update user", description = "Updates user profile fields such as email, name, bio, etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    String editPerson(@Parameter(description = "ID of the user to update", required = true) @PathVariable Long id, @RequestBody User userReq){
        //find the user being updated through ID
        Optional<User> userOptional = UserRepository.findById(id);

        //return a failure here if no user is found
        if (userOptional.isEmpty()) {
            return failure;
        }

        User user = userOptional.get();

        //update any attributes that have been added to the request
        if (userReq.getEmail() != null) {
            user.setEmail(userReq.getEmail());
        }
        if (userReq.getPasswordHash() != null) {
            user.setPasswordHash(userReq.getPasswordHash());
        }
        if (userReq.getDisplayName() != null) {
            user.setDisplayName(userReq.getDisplayName());
        }
        if (userReq.getBio() != null) {
            user.setBio(userReq.getBio());
        }
        if (userReq.getMajor() != null) {
            user.setMajor(userReq.getMajor());
        }
        if (userReq.getAge() != null) {
            user.setAge(userReq.getAge());
        }

        //save
        UserRepository.save(user);
        return success;
    }

    //s4c
    @GetMapping(path = "/login")
    @Operation(summary = "User login", description = "Authenticates a user using email and password hash.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    ResponseEntity<?> login(@Parameter(description = "User email", required = true) @RequestParam String email, @Parameter(description = "User password hash", required = true) @RequestParam String passwordHash) {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(401).body(failure);
        }

        Optional<User> userOptional = UserRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body(failure);
        }

        User user = userOptional.get();

        //return failure if the password hashes do not match up
        if (!user.getPasswordHash().equals(passwordHash)) {
            return ResponseEntity.status(401).body(failure);
        }

        return ResponseEntity.ok(user);
    }
}