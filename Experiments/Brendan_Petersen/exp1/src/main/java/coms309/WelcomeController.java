package coms309;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return "Hello and welcome to COMS 309";
    }
    
    @GetMapping("/{name}")
    public String welcome(@PathVariable String name) {
        return "Hello and welcome to COMS 309: " + name;
    }

    @GetMapping("/{one}/{two}")
    public String onetwo(@PathVariable String one, @PathVariable String two) {
        return "You are in subdirectory " + two + " of directory " + one + ".";
    }

    @GetMapping("/secret")
    public String hi() {
        return "Secret page with different message.";
    }

    // This endpoint returns an image rather than text
    @GetMapping("/image")
    public ResponseEntity<Resource> getImage() throws Exception {
        Path path = Paths.get("src/main/resources/static/images/oops.jpg");
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    // This endpoint returns a gif format image
    @GetMapping("/image/moving")
    public ResponseEntity<Resource> getGif() throws Exception {
        Path path = Paths.get("src/main/resources/static/images/warrock.gif");
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_GIF)
                .body(resource);
    }

    // This endpoint returns a magazine PDF rather than text
    @GetMapping("/pdf")
    public ResponseEntity<Resource> getPDF() throws Exception {
        Path path = Paths.get("src/main/resources/static/Interzone_025_1988-09-10.pdf");
        Resource resource = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
