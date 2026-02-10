package coms309.all;

import coms309.digimon.DigiController;
import coms309.digimon.Digimon;
import coms309.people.PeopleController;
import coms309.people.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
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
    public AllHash getAllCreatures () {
        return new AllHash(
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

    @GetMapping("/all/az")
    public List<Object> getAllCreaturesAz() {
        List<Object> creatures = new ArrayList<>();
        creatures.addAll(digiController.getAllMons().values());
        creatures.addAll(peopleController.getAllPersons().values());

        creatures.sort((o1, o2) -> {
            //if o is a Digimon, cast to Digimon and get Name, else cast to person and get last name
            String name1 = (o1 instanceof Digimon) ? ((Digimon) o1).getName() : ((Person) o1).getLastName();
            String name2 = (o2 instanceof Digimon) ? ((Digimon) o2).getName() : ((Person) o2).getLastName();
            return name1.compareTo(name2);
        });

        return creatures;
    }
}
