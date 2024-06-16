package PFE008.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ConvertController class
 * 
 * This class will be used to handle the conversion of music sheets to MIDI files.
 * The controller will take a path to a music sheet file as input.
 * It will then convert the file to a .mxl file and
 * return its path.
 * 
 * MIDI conversion is to be implemented.
 * 
 * @author Charlie Poncsak, Philippe Langevin
 * @version 2024.06.11
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

        // Convert music sheet to .mxl
        AudiverisController audiveris = new AudiverisController();
        String mxlPath = audiveris.convert(path);

        // Return error if Audiveris controller hasn't converted file
        if (mxlPath == null) {
            return new Conversion(counter.incrementAndGet(), "Error converting the file.");
        }

        System.out.println(".MXL Path : " + mxlPath);

        // Convert .mxl to .mid
        String midiPath = convertMxlToMidi(mxlPath);

        // Return error if MIDI conversion fails
        if (midiPath == null) {
            return new Conversion(counter.incrementAndGet(), "Error converting the .mxl file to .mid.");
        }

        System.out.println(".MIDI Path : " + midiPath);

        return new Conversion(counter.incrementAndGet(), "File converted. The .mid Path is : " + midiPath);
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