import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * EXAMPLE -------------------------
 * Run Audiveris from Java
 * 
 * Inputs are the music sheet file and the output directory
 * 
 * Outputs are a .mxl, .omr and .log file in the output directory
 * 
 * Setup: 
 * 1. You need to download an Audiveris dist. It should be composed of a bin and lib folder.
 * Place the bin and lib folders in the right directory:
 * PFE008/audiveris/dist
 * 
 * 2. You then need a music sheet file. It can be specified in the inputFile variable. In this example, we use:
 * PFE008/In/Minecraft Theme.pdf
 * 
 * 3. You can run the program (Main.java). It will output the files in this directory:
 * PFE008/Out
 * 
 * @author Charlie Poncsak
 * @version 2024.06.05
 */
public class Main {
    public static void main(String[] args) {
        try {
            String workingDir = System.getProperty("user.dir");

            // Path to Audiveris
            String audiverisPath = workingDir + "/audiveris/dist/bin/Audiveris.bat";
            
            // Input file (example)
            String inputFile = '\"' + workingDir + "/In/Minecraft Theme.pdf\"";
            
            // Output directory
            String outputDir = workingDir + "\"/Out\"";
            
            String command = audiverisPath + " -batch -export -output " + outputDir + " -- " + inputFile;
            
            // Running directly From bin gives Gradle errors, so we run from a dist. Here's the bin command just in case
            
            /*
            // Path to Gradlew
            String gradlewPath = workingDir + "/audiveris/bin/gradlew.bat";
            String command = gradlewPath + " run -PcmdLineArgs=\"-batch,-export,-output," + outputDir + ",--," + inputFile + "\""; 
            */

            // Run the command
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
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}