package coms309.all;

import coms309.digimon.Digimon;
import coms309.people.Person;

import java.util.HashMap;
import java.util.List;

public class All {
    private HashMap<String, Digimon> digimon;
    private HashMap<String, Person> people;

    public All(HashMap<String, Digimon> digimon, HashMap<String, Person> people) {
        this.digimon = digimon;
        this.people = people;
    }

    public HashMap<String, Digimon> getAllMons() {
        return digimon;
    }
    public HashMap<String, Person> getAllPeople() {
        return people;
    }
}

