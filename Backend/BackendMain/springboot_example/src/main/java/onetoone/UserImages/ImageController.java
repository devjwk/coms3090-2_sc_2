package onetoone.UserImages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import onetoone.GroupMember.GMController;
import onetoone.GroupMember.GMRepository;
import onetoone.GroupMember.GroupMember;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ImageController {
    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    record userImageInfo(Long imageId, String imageLink) {}

    //return all images of a user
    @GetMapping(path = "/image/user/{id}")
    @Operation(summary = "List user images", description = "Lists the images a user has uploaded using user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    ResponseEntity<List<ImageController.userImageInfo>> listUserImages(@PathVariable Long id) {
        List<Image> images = imageRepository.findByUserId_UserId(id);

        if (images.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ImageController.userImageInfo> what = images.stream()
                .map(soundwave -> new ImageController.userImageInfo(
                        soundwave.getImageId(),
                        soundwave.getImageLink()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(what);
    }


    //return images based on ID
    @GetMapping(path = "/image/{id}")
    public ResponseEntity<String> imageLink(@PathVariable Long id) {
        if(!imageRepository.existsById(id)){
            return ResponseEntity.status(404).body("Image not found");
        }

        Image image = imageRepository.findByImageId(id);

        return ResponseEntity.ok(image.getImageLink());
    }

    //delete image
    //only deletes the table listing, not the actual image
    @DeleteMapping(path = "/image/{id}")
    public ResponseEntity<String> deleteImage(@PathVariable Long id) {
        if(!imageRepository.existsById(id)){
            return ResponseEntity.status(404).body("Image not found");
        }

        imageRepository.deleteById(id);
        return ResponseEntity.ok("Image deleted");
    }

}
