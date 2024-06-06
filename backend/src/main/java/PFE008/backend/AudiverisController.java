package PFE008.backend;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Controller for Audiveris
 * 
 * This controller will handle the conversion of music sheets to .mxl files.
 * The controller will take a path to a music sheet file as input.
 * It will then convert the file to a .mxl file and
 * return its path.
 * 
 * @author Charlie Poncsak
 * @version 2024.06.06
 */
public class AudiverisController {
    
    /**
     * Convert a music sheet to a .mxl file
     * 
     * @param path Path to the music sheet file
     * @return The .mxl path if it has been created, an error message otherwise
     */
    public String convert(String path) {
        String workingDir = System.getProperty("user.dir");
        System.out.println("Working Directory = " + workingDir);
        
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
            return "Error converting file: " + e.getMessage();
        }

        // Return the .mxl path
        return workingDir + "/Out" + path.substring(path.lastIndexOf('/'), path.lastIndexOf('.')) + ".mxl";
    }
}
