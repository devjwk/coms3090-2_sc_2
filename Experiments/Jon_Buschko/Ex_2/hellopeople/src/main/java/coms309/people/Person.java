package coms309.people;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Provides the Definition/Structure for the people row
 *
 * @author Vivek Bengre
 */
@Getter // Lombok Shortcut for generating getter methods (Matches variable names set ie firstName -> getFirstName)
@Setter // Similarly for setters as well
@NoArgsConstructor // Default constructor
public class Person {

    private String firstName;

    private String lastName;

    private String address;

    private String telephone;

    private String vibe;

    private List<String> interests;


//    public Person(){
//
//    }

    public Person(String firstName, String lastName, String address, String telephone, String vibe, List<String> interests){
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.telephone = telephone;
        this.vibe = vibe;
        this.interests = interests;
    }


    /**
     * Getter and Setters below are technically redundant and can be removed.
     * They will be generated from the @Getter and @Setter tags above class
     */

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getVibe() {
        return this.vibe;
    }

    public void setVibe(String vibe) {
        this.vibe = vibe;
    }

    public List<String> getInterests() {
        return this.interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    @Override
    public String toString() {
        return firstName + " "
                + lastName + " "
                + address + " "
                + telephone + " "
                + vibe + " "
                + interests;
    }
}
