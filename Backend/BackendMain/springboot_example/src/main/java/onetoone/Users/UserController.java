package onetoone.Users;
import java.util.List;
import java.util.Optional;

import onetoone.Persons.Person;
import onetoone.Persons.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


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
    //Logging in
    @GetMapping(path = "/login")
    String login(@RequestBody User userReq) {
        //find the user through the email they inputted
        Optional<User> userOptional = UserRepository.findByEmail(userReq.getEmail());

        //return a failure here if the findByEmail doesn't find anything
        if (userOptional.isEmpty()) {
            return failure;
        }

        User user = userOptional.get();

        //return failure if the password hashes do not match up
        if (!user.getPasswordHash().equals(userReq.getPasswordHash())) {
            return failure;
        }

        //success if login was successful
        return success;
    }
}