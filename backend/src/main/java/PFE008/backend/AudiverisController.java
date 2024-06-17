package PFE008.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Controller for Audiveris
 * 
 * This controller will handle the conversion of music sheets to .mxl files.
 * The controller will take a path to a music sheet file as input.
 * It will then convert the file to a .mxl file and
 * return its path.
 * 
 * @author Charlie Poncsak, modified by Philippe Langevin
 * @version 2024.06.17
 */
public class AudiverisController {
    
    /**
     * Convert a music sheet to a .mid file
     * 
     * @param path Path to the music sheet file
     * @return The .mid path if it has been created, null otherwise
     */
    public String convert(String path) {
        String workingDir = System.getProperty("user.dir");
        
        String audiverisPath = workingDir + "/audiveris/dist/bin/Audiveris.bat";
        String inputFile = '\"' + path + "\"";
        String outputDir = workingDir + "/Out";
        String command = audiverisPath + " -batch -export -output " + outputDir + " -- " + inputFile;
        
        // Run the command
        try {
            System.out.println("Running Audiveris..\n");
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = processBuilder.start();

            // Read the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Audiveris process exited with code: " + exitCode);
        } catch (Exception e) {
            return null;
        }

        String mxlPath = workingDir + "/Out" + path.substring(path.lastIndexOf('\\'), path.lastIndexOf('.')) + ".mxl";
        
        // Checking if file got converted
        if (!new File(mxlPath).exists()) {
            return null;
        }

        // Convert .mxl to .mid
        String midiPath = convertMxlToMidi(mxlPath);

        // Return the .mid path
        if (midiPath != null && new File(midiPath).exists()) {
            return midiPath;
        }

        return null;
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