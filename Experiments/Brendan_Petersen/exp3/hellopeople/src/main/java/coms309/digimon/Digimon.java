package coms309.digimon;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Digimon {
    private String name;
    private String attribute;
    private String email;

    public Digimon(String name, String attribute, String email){
        this.name = name;
        this.attribute = attribute;
        this.email = email;
    }

    @Override
    public String toString() {
        return name + " "
                + attribute + " "
                + email;
    }
}

//Example digi #1: { "name" : "Silphymon", "attribute" : "Free", "email" : "silphy@sb.ntt.jp" }
//Example digi #2: { "name" : "Blackwargreymon", "attribute" : "Virus", "email" : "bwg@sb.ntt.jp" }
//Example digi #2: { "name" : "Gaomon", "attribute" : "Data", "email" : "gao@sb.ntt.jp" }
