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
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.StandardCopyOption;

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

    /*
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
     */

    //delete image
    @DeleteMapping(path = "/image/{id}")
    public ResponseEntity<String> deleteImage(@PathVariable Long id) {
        if (!imageRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Image not found");
        }

        Image image = imageRepository.findByImageId(id);

        String imageLink = image.getImageLink();
        Path filePath = Paths.get("/home" + imageLink);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to delete image file: " + e.getMessage());
        }

        imageRepository.deleteById(id);
        return ResponseEntity.ok("Image deleted");
    }

    @PostMapping(path = "/image/user/{id}")
    public ResponseEntity<String> uploadImage(@PathVariable Long id,
                                              @RequestParam("file") MultipartFile file) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(404).body("User not found");
        }

        if (file.isEmpty()) {
            return ResponseEntity.status(400).body("No file provided");
        }

        try {
            Path userDir = Paths.get("/home/files/images/" + id);
            Files.createDirectories(userDir);

            String filename = file.getOriginalFilename();
            Path filePath = userDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageLink = "/files/images/" + id + "/" + filename;
            Image image = new Image();
            image.setUserId(userRepository.findById(id).get());
            image.setImageLink(imageLink);
            imageRepository.save(image);

            return ResponseEntity.ok(imageLink);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}
