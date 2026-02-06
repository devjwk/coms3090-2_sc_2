package coms309.all;

import coms309.digimon.Digimon;
import coms309.people.Person;

import java.util.List;

public class AllList {
    private List<Person> people;
    private List<Digimon> digimon;
    
    public AllList(List<Person> people, List<Digimon> digimon) {
        this.people = people;
        this.digimon = digimon;
    }
    
    public List<Person> getPeople() {
        return people;
    }
    
    public List<Digimon> getMons() {
        return digimon;
    }
}
