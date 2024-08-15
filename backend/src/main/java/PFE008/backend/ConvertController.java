import PFE008.backend.FileUtil;
import PFE008.backend.AudiverisController;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.IOException;


@RestController
public class ConvertController {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PostMapping("/convert")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> convert(@RequestParam("file") MultipartFile multipartFile,
                                     @RequestParam(value = "tempos", required = false) String tempos) {
        try {
            // Validate input file
            if (multipartFile.isEmpty()) {
                return new ResponseEntity<>("Please select a file", HttpStatus.BAD_REQUEST);
            }

            if (multipartFile.getSize() > 10 * 1024 * 1024) { // 10 MB limit
                return new ResponseEntity<>("File is too large", HttpStatus.BAD_REQUEST);
            }

            if (!FileUtil.isValidFile(multipartFile)) {
                return new ResponseEntity<>("Invalid file type. Allowed types: PDF, JPG, JPEG, PNG", HttpStatus.BAD_REQUEST);
            }

            // Save input file
            String fileExtension = "." + StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
            Path filePath = FileUtil.saveFile(fileExtension, multipartFile, "In");

            System.out.println("Received tempos: " + tempos);

            // Convert music sheet to .mxl
            AudiverisController audiveris = new AudiverisController(tempos);
            String midiPath = audiveris.convert(filePath.toString());

            if (midiPath == null) {
                return new ResponseEntity<String>("Could not convert file", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Schedule cleanup of directories after a delay
            executorService.submit(() -> cleanupDirectoriesWithDelay(filePath));

            // Build response
            return buildFileResponse(midiPath);

        } catch (IOException e) {
            return new ResponseEntity<String>("An error occurred during file processing: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void cleanupDirectoriesWithDelay(Path filePath) {
        try {
            TimeUnit.SECONDS.sleep(3);
            String workingDir = System.getProperty("user.dir");
            String filePrefix = filePath.getFileName().toString().replaceFirst("[.][^.]+$", ""); // Remove file extension
            FileUtil.cleanupDirectories(workingDir + "\\In", workingDir + "\\Out", filePrefix);
            System.out.println("Cleanup completed for files starting with: " + filePrefix);
        } catch (InterruptedException e) {
            System.err.println("Cleanup interrupted: " + e.getMessage());
        }
    }

    private ResponseEntity<Resource> buildFileResponse(String midiPath) {
        try {
            Resource resource = new FileUtil().getFileAsResource(midiPath);

            if (resource == null) {
                return new ResponseEntity<>("Converted file not found", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String contentType = "application/octet-stream";
            String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
