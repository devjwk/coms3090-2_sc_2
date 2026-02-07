package coms309.digimon;

import coms309.people.Person;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class DigiController {
    HashMap<String, Digimon> digiList = new HashMap<>();

    //Listing, get
    @GetMapping("/digimon")
    public HashMap<String, Digimon> getAllMons() {
        return digiList;
    }

    //Listing in alphabetical order, get (New Endpoint)
    @GetMapping("/digimon/az")
    public List<Digimon> getAllMonsAz() {
        List<Digimon> res = new ArrayList<>(digiList.values());
        res.sort(Comparator.comparing(Digimon::getName, String.CASE_INSENSITIVE_ORDER));
        return res;
    }

    //Create, post
    @PostMapping("/digimon")
    public String createDigi(@RequestBody Digimon digimon) {
        System.out.println(digimon);
        digiList.put(digimon.getName(), digimon);
        return "New Digimon " + digimon.getName() + " Saved";
    }

    //Read, get
    @GetMapping("/digimon/{name}")
    public Digimon getDigi(@PathVariable String name) {
        return digiList.get(name);
    }

    //Update, put
    @PutMapping("/digimon/{name}")
    public Digimon updateDigi(@PathVariable String name, @RequestBody Digimon d) {
        digiList.replace(name, d);
        return digiList.get(name);
    }

    //Search by name, get
    //Fixed to not be case-sensitive
    @GetMapping("/digimon/contains/n")
    public List<Digimon> getDigiByParam(@RequestParam("name") String name) {
        List<Digimon> res = new ArrayList<>();
        for (Digimon d : digiList.values()) {
            if (d.getName().toLowerCase().contains(name.toLowerCase()))
                res.add(d);
        }
        return res;
    }

    //Search by attribute, get (New Endpoint)
    //Fixed to not be case-sensitive
    @GetMapping("/digimon/contains/a")
    public List<Digimon> getDigiByAtt(@RequestParam("attribute") String attribute) {
        List<Digimon> res = new ArrayList<>();
        for (Digimon d : digiList.values()) {
            if (d.getAttribute().toLowerCase().contains(attribute.toLowerCase()))
                res.add(d);
        }
        return res;
    }

    //Delete, delete
    @DeleteMapping("/digimon/{name}")
    public HashMap<String, Digimon> deleteDigi(@PathVariable String name) {
        digiList.remove(name);
        return digiList;
    }
}
