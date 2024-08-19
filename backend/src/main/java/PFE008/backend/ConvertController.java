package PFE008.backend;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * ConvertController class
 * 
 * This class will be used to handle the conversion of music sheets to MIDI files.
 * The controller will take a path to a music sheet file as input.
 * It will then convert the file to a .mxl file and
 * return its path.
 * 
 * 
 * @author Charlie Poncsak, Philippe Langevin
 * @version 2024.06.17
 */
@RestController
public class ConvertController {

    @Operation(summary = "Convert a music sheet to MIDI", description = "Accepts a file (PDF, JPG, JPEG, PNG) and converts it to a MIDI file using Audiveris.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "File converted successfully", content = @Content(schema = @Schema(implementation = Resource.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input file or file too large", content = @Content),
        @ApiResponse(responseCode = "500", description = "File conversion failed or unexpected error", content = @Content)
    })
    @PostMapping("/convert")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> convert(@RequestParam("file") MultipartFile multipartFile,
                                     @RequestParam(value = "tempos", required = false) String tempos) {
        try {
            // Validate input file
            if (multipartFile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No file selected. Please select a file to upload.");
            }

            // Check file size (under 10MB)
            if (multipartFile.getSize() > 10485760) { // 10MB
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("File is too large. The maximum allowed file size is 10MB.");
            }

            // Check the file signature
            if (!FileUtil.isValidFile(multipartFile)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid file type. Accepted file types are: PDF, JPG, JPEG, PNG.");
            }

            // Save input file
            String fileExtension = "." + StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
            Path filePath = FileUtil.saveFile(fileExtension, multipartFile, "In");
            System.out.println("Received tempos: " + tempos);

            // Convert music sheet to .mxl
            AudiverisController audiveris = new AudiverisController(tempos);
            String midiPath = audiveris.convert(filePath.toString());

            if (midiPath == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("File conversion failed. Please check the input file and try again.");
            }

            // Start a new thread for cleaning up the directories after a delay
            new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    String workingDir = System.getProperty("user.dir");
                    System.out.println("Cleaning up directories - Deleting files starting with: " + filePath.getFileName().toString());
                    FileUtil.cleanupDirectories(workingDir + "\\In", workingDir + "\\Out", filePath.getFileName().toString());
                } catch (InterruptedException e) {
                    System.out.println("Interrupted while waiting to clean up directories: " + e.getMessage());
                }
            }).start();

            // Build response
            Resource resource;
            try {
                resource = FileUtil.getFileAsResource(midiPath);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to retrieve the converted file. Please try again later.");
            }

            if (resource == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Converted file not found. Please try again later.");
            }

            String contentType = "application/octet-stream";
            String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is up and running");
    }
}
