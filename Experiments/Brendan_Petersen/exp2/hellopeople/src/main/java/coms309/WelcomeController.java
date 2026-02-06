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
        return "Hello and welcome to COMS 309";
    }
}

/**
 * Changes:
 * -Second category added, called "Digimon"
 *      -Largely implements same functions, though some are missing
 *      -Different information than People to reflect different needs
 * -"All" package created, for shared functions between People and Digimon
 *      -List function at '/all'
 *      -Search function at '/all/contains'
 *      -@Autowired used to link data so AllController can access it
 *      -Two classes created, "All" and "AllList", to support AllController functions
 *          -All used for listing (HashMaps)
 *          -AllList used for searching (Lists)
 */