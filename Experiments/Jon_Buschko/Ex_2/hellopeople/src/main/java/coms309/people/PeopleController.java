package coms309.people;

import org.springframework.web.bind.annotation.*;


import java.util.*;

/**
 * Controller used to showcase Create and Read from a LIST
 *
 * @author Vivek Bengre
 */

@RestController
public class PeopleController {

    // Note that there is only ONE instance of PeopleController in 
    // Springboot system.
    HashMap<String, Person> peopleList = new  HashMap<>();

    //CRUDL (create/read/update/delete/list)
    // use POST, GET, PUT, DELETE, GET methods for CRUDL

    // THIS IS THE LIST OPERATION
    // gets all the people in the list and returns it in JSON format
    // This controller takes no input. 
    // Springboot automatically converts the list to JSON format 
    // in this case because of @ResponseBody
    // Note: To LIST, we use the GET method
    @GetMapping("/people")
    public  HashMap<String,Person> getAllPersons() {
        return peopleList;
    }

    // THIS IS THE CREATE OPERATION
    // springboot automatically converts JSON input into a person object and 
    // the method below enters it into the list.
    // It returns a string message in THIS example.
    // Note: To CREATE we use POST method
    @PostMapping("/people")
    public String createPerson(@RequestBody Person person) {
        System.out.println(person);
        peopleList.put(person.getFirstName(), person);
        String s = "New person "+ person.getFirstName() + " Saved";
        return s;
        //public  ResponseEntity<Map<String, String>>  //unused
        // createPerson(@RequestBody Person person) { // unused
        //Map <String, String> body = new HashMap<>();// unused
        //body.put("message", s); // unused
        //ResponseEntity<>(body, HttpStatus.OK); // unused
    }

    // THIS IS THE READ OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We extract the person from the HashMap.
    // springboot automatically converts Person to JSON format when we return it
    // Note: To READ we use GET method
    @GetMapping("/people/{firstName}")
    public Person getPerson(@PathVariable String firstName) {
        Person p = peopleList.get(firstName);
        return p;
    }

    // THIS IS A GET METHOD
    // RequestParam is expected from the request under the key "name"
    // returns all names that contains value passed to the key "name"
    @GetMapping("/people/contains")
    public List<Person> getPersonByParam(@RequestParam("name") String name) {
        List<Person> res = new ArrayList<>(); 
        for (Person p : peopleList.values()) {
            if (p.getFirstName().contains(name) || p.getLastName().contains(name))
                res.add(p);
        }
        return res;
    }

    // THIS IS THE UPDATE OPERATION
    // We extract the person from the HashMap and modify it.
    // Springboot automatically converts the Person to JSON format
    // Springboot gets the PATHVARIABLE from the URL
    // Here we are returning what we sent to the method
    // Note: To UPDATE we use PUT method
    @PutMapping("/people/{firstName}")
    public Person updatePerson(@PathVariable String firstName, @RequestBody Person p) {
        peopleList.replace(firstName, p);
        return peopleList.get(firstName);
    }


    // THIS IS THE DELETE OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We return the entire list -- converted to JSON
    // Note: To DELETE we use delete method
    
    @DeleteMapping("/people/{firstName}")
    public HashMap<String, Person> deletePerson(@PathVariable String firstName) {
        peopleList.remove(firstName);
        return peopleList;
    }

    @DeleteMapping("/people")
    public String deleteAllPeople() {
        peopleList.clear();
        return "Deleted all people";
    }

    // Searches for people based on the city they live in
    @GetMapping("/people/city")
    public List<Person> getByCity(@RequestParam("city") String city) {
        List<Person> res = new ArrayList<>();
        for (Person p : peopleList.values()) {
            if (p.getAddress().contains(city))
                res.add(p);
        }
        return res;
    }

    // Computes the compatibility score of every person based on the input name
    // Then returns the top 3 most compatible people in comparison to the input
    @GetMapping("/people/match/{name}")
    public List<Map<String, Object>> topMatches(@PathVariable String name){
        List<Map<String, Object>> res = new ArrayList<>();

        Person PersonA = peopleList.get(name);

        // Returns null instead of erroring when a non-existent person is input
        if (PersonA == null) return res;

        for (Person p : peopleList.values()) {
            // Don't compare a person to themself
            if(p.getFirstName().equals(name) || p.getLastName().equals(PersonA.getLastName())) continue;

            int score = 0;

            if (PersonA.getAddress().equalsIgnoreCase(p.getAddress())) score += 50;
            if (PersonA.getVibe().equalsIgnoreCase(p.getVibe())) score += 20;
            if (PersonA.getInterests().stream().anyMatch(p.getInterests()::contains)) score += 30;

            Map<String, Object> match = new HashMap<>();
            match.put("name", p.getFirstName());
            match.put("score", score);

            res.add(match);
        }

        res.sort((a, b) -> (int)b.get("score") - (int)a.get("score"));

        return res;
    }

    // Compare the match potential of 2 specific people
    @GetMapping("/people/match/{p1}/{p2}")
    public LinkedHashMap<String, Object> getMatch(@PathVariable String p1, @PathVariable String p2) {
        Person Person1 = peopleList.get(p1);
        Person Person2 = peopleList.get(p2);
        int score = 0;
        if (Person1 == null || Person2 == null) return null;

        boolean sameCity = (Person1.getAddress()).equalsIgnoreCase(Person2.getAddress());

        if (sameCity) score += 50;
        if (Person1.getVibe().equalsIgnoreCase(Person2.getVibe())) score += 20;
        if (Person1.getInterests().stream().anyMatch(Person2.getInterests()::contains)) {
            score += 30;
        }
        LinkedHashMap match = new LinkedHashMap();

        match.put("PersonA", Person1.getFirstName());
        match.put("PersonB", Person2.getFirstName());
        match.put("SameCity", sameCity);
        match.put("VibeA", Person1.getVibe());
        match.put("VibeB", Person2.getVibe());
        match.put("Compatibility%", score);
        return match;
    }

    // Auto creates a few people for testing
    @PostMapping("/people/seed")
    public List<Person> createExamplePeople(){
        List<Person> seeded = new ArrayList<>();

        Person p1 = new Person();
        p1.setFirstName("JJ");
        p1.setLastName("Buschko");
        p1.setAddress("Ames");
        p1.setVibe("Chill");
        p1.setInterests(new ArrayList<>(List.of("lifting", "compression", "space")));

        Person p2 = new Person();
        p2.setFirstName("Alex");
        p2.setLastName("Smith");
        p2.setAddress("Ames");
        p2.setVibe("Chill");
        p2.setInterests(new ArrayList<>(List.of("gaming", "space", "coding")));

        Person p3 = new Person();
        p3.setFirstName("Taylor");
        p3.setLastName("Jones");
        p3.setAddress("Des Moines");
        p3.setVibe("Energetic");
        p3.setInterests(new ArrayList<>(List.of("running", "music", "lifting")));

        Person p4 = new Person();
        p4.setFirstName("Sam");
        p4.setLastName("Lee");
        p4.setAddress("Ames");
        p4.setVibe("Calm");
        p4.setInterests(new ArrayList<>(List.of("reading", "coding", "space")));

        List<Person> demo = List.of(p1, p2, p3, p4);

        for (Person p : demo) {
            peopleList.put(p.getFirstName(), p);
            seeded.add(p);
        }

        return seeded;
    }
} // end of people controller
