package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;

@RestController
class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return "COMS 309 Friendship Finder API is running";
    }
    
    @GetMapping("/hello/{name}")
    public String welcome(@PathVariable String name) {
        return "Hello " + name + " welcome to our Friendship Finder";
    }


    @GetMapping("/hello")
    public String welcomeparam(@RequestParam String name,
    @RequestParam(required = false, defaultValue = "chill hangouts") String vibe) {
        return "Hey " + name + "! Looking for friends who like: " + vibe;
    }

    @GetMapping("/profile/{username}")
    public ProfilePreview profile(@PathVariable String username) {
        return new ProfilePreview(
                username,
                "CS student looking for friends",
                List.of("gym", "coding", "gaming"),
                LocalDateTime.now().toString()
        );
    }
}

record ProfilePreview(String username, String bio, List<String> interests, String createdAt) {}