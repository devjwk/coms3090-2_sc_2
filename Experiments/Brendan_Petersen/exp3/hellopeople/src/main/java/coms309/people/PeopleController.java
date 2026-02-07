package coms309.people;

import coms309.digimon.Digimon;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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
    // gets coms309.all the people in the list and returns it in JSON format
    // This controller takes no input. 
    // Springboot automatically converts the list to JSON format 
    // in this case because of @ResponseBody
    // Note: To LIST, we use the GET method
    @GetMapping("/people")
    public  HashMap<String,Person> getAllPersons() {
        return peopleList;
    }

    //Listing in alphabetical order (last names), get (New Endpoint)
    @GetMapping("/people/az/last")
    public List<Person> getAllPersonsAzL() {
        List<Person> res = new ArrayList<>(peopleList.values());
        res.sort(Comparator.comparing(Person::getLastName, String.CASE_INSENSITIVE_ORDER));
        return res;
    }

    //Listing in alphabetical order (first names), get (New Endpoint)
    @GetMapping("/people/az/first")
    public List<Person> getAllPersonsAzF() {
        List<Person> res = new ArrayList<>(peopleList.values());
        res.sort(Comparator.comparing(Person::getFirstName, String.CASE_INSENSITIVE_ORDER));
        return res;
    }

    // THIS IS THE CREATE OPERATION
    // springboot automatically converts JSON input into a person object and 
    // the method below enters it into the list.
    // It returns a string message in THIS example.
    // Note: To CREATE we use POST method
    @PostMapping("/people")
    public  String createPerson(@RequestBody Person person) {
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
    // returns coms309.all names that contains value passed to the key "name"
    @GetMapping("/people/contains/n")
    public List<Person> getPersonByParam(@RequestParam("name") String name) {
        List<Person> res = new ArrayList<>(); 
        for (Person p : peopleList.values()) {
            if (p.getFirstName().toLowerCase().contains(name.toLowerCase()) || p.getLastName().toLowerCase().contains(name.toLowerCase()))
                res.add(p);
        }
        return res;
    }

    //Search by telephone, get (New Endpoint)
    @GetMapping("/people/contains/t")
    public List<Person> getPersonByTel(@RequestParam("telephone") String telephone) {
        List<Person> res = new ArrayList<>();
        for (Person p : peopleList.values()) {
            if (p.getTelephone().contains(telephone))
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
} // end of people controller

