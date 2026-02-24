package onetoone.Users;
import java.util.List;

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
    String editPerson(@PathVariable Long id){
        //TODO
        UserRepository.editPerson; //?
    }

    //s4c
    //Logging in
    @GetMapping(path = "/users/login")
    String login(@RequestBody User userReq) {
        //INCOMPLETE

        //find the user through the email they inputed
        Optional<User> userOptional = UserRepository.findByEmail(userReq.getEmail());
        User user = userOptional.get();

        //return failure if the password hashes do not match up
        if (!user.getPasswordHash().equals(userReq.getPasswordHash())) {
            return failure;
        }

        //x

        //success if login was successful
        return success;
    }
}