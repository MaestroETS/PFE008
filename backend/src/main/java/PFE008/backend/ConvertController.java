package PFE008.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/convert")
    public Conversion convert(@RequestParam(value = "path", defaultValue = "") String path) {
        
        // Check if path is empty
        if (path.isEmpty()) {
            return new Conversion(counter.incrementAndGet(), "No path specified!");
        }

        // Check if path is valid and file exists
        if (!new File(path).exists()) {
            return new Conversion(counter.incrementAndGet(), "File does not exist!");
        }

        // Convert music sheet to .mid
        AudiverisController audiveris = new AudiverisController();
        String midiPath = audiveris.convert(path);

        // Return error if Audiveris controller hasn't converted file
        if (midiPath == null) {
            return new Conversion(counter.incrementAndGet(), "Error converting the file.");
        }

        System.out.println(".MIDI Path : " + midiPath);

        return new Conversion(counter.incrementAndGet(), "File converted. The .mid Path is : " + midiPath);
    }

    @PostMapping("/convert")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> convert(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        FileUtil downloadUtil = new FileUtil();
        
        // Save input file
        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        Path filePath = FileUtil.saveFile(fileName, multipartFile, "In");
        
        // Convert music sheet to .mid
        AudiverisController audiveris = new AudiverisController();
        String midiPath = audiveris.convert(filePath.toString());

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

    private String convertMxlToMidi(String mxlPath) {
        String pythonScriptPath = System.getProperty("user.dir") + "/java/PFE008/backend/MxlToMidi.py";
        String command = "python " + pythonScriptPath + " " + mxlPath;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = processBuilder.start();

            // Read the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Python script process exited with code: " + exitCode);

            if (exitCode != 0) {
                return null;
            }

            // Generate the output MIDI file path
            String midiPath = mxlPath.substring(0, mxlPath.lastIndexOf('.')) + ".mid";
            return midiPath;

        } catch (Exception e) {
            System.out.println("Error converting .mxl to .mid: " + e.getMessage());
            return null;
        }
    }
}
