package coms309.digimon;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;

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

    @PutMapping("/digimon/{name}")
    public Digimon updateDigi(@PathVariable String name, @RequestBody Digimon d) {
        digiList.replace(name, d);
        return digiList.get(name);
    }

    //Delete, delete
    @DeleteMapping("/digimon/{name}")
    public HashMap<String, Digimon> deleteDigi(@PathVariable String name) {
        digiList.remove(name);
        return digiList;
    }
}
