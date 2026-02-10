package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple Hello World Controller to display the string returned
 *
 * @author Vivek Bengre
 */

@RestController
class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return "Default page";
    }
}

//comment

/**
 * Updates from exp2:
 * -Added login requirement (Username is "user" , password is "password")
 * -Added the error page from exp1
 *      -image is now local instead of pulled from web
 *      -different image is now used
 * -Class "All" renamed to "AllHash" to be more descriptive, similar to "AllList"
 * -WelcomeController message changed to "Default page"
 * -Digimon search function, which was missing in exp2, added at /digimon/contains?name=
 */

/**
 * Updates from original demo1:
 *
 * Digimon:
 * -Search by attribute /digimon/contains/a
 * ---(name is now /digimon/contains/n)
 * -List alphabetically (name) /digimon/az
 *
 * Person:
 * -Search by telephone /people/contains/t
 * ---(name is now /people/contains/n)
 * -List alphabetically (first name) /people/az/first
 * -List alphabetically (last name) /people/az/last
 *
 * All:
 * -List alphabetically (results intermixed) /all/az
 * ---This uses last name for humans
 */
