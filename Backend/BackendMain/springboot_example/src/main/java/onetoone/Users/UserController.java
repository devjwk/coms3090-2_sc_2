package onetoone.Users;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@RestController
public class UserController {

    @Autowired
    UserRepository UserRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";



    @PostMapping(path = "/users")
    String createUser(@RequestBody User User){
        if (User == null)
            return failure;
        UserRepository.save(User);
        return success;
    }

    @DeleteMapping(path = "/users/{id}")
    String deletePerson(@PathVariable Long id){
        UserRepository.deleteById(id);
        return success;
    }

    //s4c
    //Edit profile
    @PutMapping(path = "/users/edit/{id}")
    String editPerson(@PathVariable Long id, @RequestBody User userReq){
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
    ResponseEntity<?> login(@RequestParam String email, @RequestParam String passwordHash) {
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