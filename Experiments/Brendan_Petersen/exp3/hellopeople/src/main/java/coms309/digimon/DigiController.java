package coms309.digimon;

import coms309.people.Person;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class DigiController {
    HashMap<String, Digimon> digiList = new HashMap<>();

    //Listing, get
    @GetMapping("/digimon")
    public HashMap<String, Digimon> getAllMons() {
        return digiList;
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
    @GetMapping("/digimon/contains")
    public List<Digimon> getDigiByParam(@RequestParam("name") String name) {
        List<Digimon> res = new ArrayList<>();
        for (Digimon d : digiList.values()) {
            if (d.getName().contains(name))
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
