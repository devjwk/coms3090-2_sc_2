package coms309.all;

import coms309.digimon.DigiController;
import coms309.digimon.Digimon;
import coms309.people.PeopleController;
import coms309.people.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class AllController {
    //https://www.geeksforgeeks.org/springboot/spring-autowired-annotation/
    //Add ("inject") dependencies using At Autowired
    @Autowired
    private DigiController digiController;
    @Autowired
    private PeopleController peopleController;

    @GetMapping("/all")
    public All getAllCreatures () {
        return new All(
                digiController.getAllMons(),
                peopleController.getAllPersons()
        );
    }

    @GetMapping("/all/contains")
    public AllList getCreatureByParam(@RequestParam("name") String name) {
        List<Person> resp = new ArrayList<>();
        List<Digimon> resd = new ArrayList<>();

        for (Person p : peopleController.getAllPersons().values()) {
            if (p.getFirstName().contains(name) || p.getLastName().contains(name))
                resp.add(p);
        }

        for (Digimon d : digiController.getAllMons().values()) {
            if (d.getName().contains(name)) {
                resd.add(d);
            }
        }

        return new AllList(resp, resd);
    }
}
