package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

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
}
