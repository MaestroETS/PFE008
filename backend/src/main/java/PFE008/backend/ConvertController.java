package PFE008.backend;

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

    @PostMapping("/convert")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> convert(@RequestParam("file") MultipartFile multipartFile,
                                     @RequestParam(value = "tempos", required = false) String tempos) throws IOException {
        FileUtil downloadUtil = new FileUtil();
        
        // Validate input file
        if (multipartFile.isEmpty()) {
            return new ResponseEntity<>("Please select a file", HttpStatus.BAD_REQUEST);
        }

        // check if under 10MB
        if (multipartFile.getSize() > 10485760) {
            return new ResponseEntity<>("File is too large", HttpStatus.BAD_REQUEST);
        }

        // check the file signature
        if (!FileUtil.isValidFile(multipartFile)) {
            return new ResponseEntity<>("File is invalid. Must be a pdf, jpg, jpeg or png", HttpStatus.BAD_REQUEST);
        }

        // Save input file
        String fileExtension = "." + StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
        Path filePath = FileUtil.saveFile(fileExtension, multipartFile, "In");
        System.out.println("Received tempos: " + tempos);
        
        // Convert music sheet to .mxl
        AudiverisController audiveris = new AudiverisController(tempos);
        String midiPath = audiveris.convert(filePath.toString());

        // Start a new thread for cleaning up the directories after a delay
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
                String workingDir = System.getProperty("user.dir");
                System.out.println("Cleaning up directories - Deleting files starting with: " + filePath.toString().substring(filePath.toString().lastIndexOf('\\') + 1, filePath.toString().lastIndexOf('.')));
                FileUtil.cleanupDirectories(workingDir + "\\In", workingDir + "\\Out", filePath.toString().substring(filePath.toString().lastIndexOf('\\') + 1, filePath.toString().lastIndexOf('.')));
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting to clean up directories: " + e.getMessage());
            }
        }).start();

        if (midiPath == null) {
            return new ResponseEntity<>("Could not convert file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
         
        // Build response
        Resource resource = null;
        try {
            resource = downloadUtil.getFileAsResource(midiPath);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
         
        if (resource == null) {
            return new ResponseEntity<>("Could not find converted file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
         
        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
         
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
            .body(resource); 
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is up and running");
    }
}
