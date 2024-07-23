package PFE008.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Controller for Audiveris
 * 
 * This controller will handle the conversion of music sheets to .mxl files.
 * The controller will take a path to a music sheet file as input.
 * It will then convert the file to a .mxl file and
 * return its path.
 * 
 * @author Charlie Poncsak, modified by Philippe Langevin, Xavier Jeanson
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
        System.out.println("Working Directory: " + workingDir);

        String audiverisPath = workingDir + "/audiveris/dist/bin/Audiveris.bat";
        String inputFile = '\"' + path + "\"";
        String outputDir = workingDir + "\\Out";
        String[] options = new String[]{"org.audiveris.omr.sheet.BookManager.useCompression=false", "smallHeads=true", "smallBeams=true", "multiWholeHeadChords=true "};
        String commandMXL = audiverisPath + " -batch -export -option " + options[1] + " -option " + options[2] + " -option " + options[3] + " -output " + outputDir + " -- " + inputFile;

        System.out.println("Audiveris commandMXL: " + commandMXL);

        // Run the command
        try {
            System.out.println("Running Audiveris..");
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", commandMXL);
            Process process = processBuilder.start();

            // Read the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Audiveris process exited with code: " + exitCode);

            if (exitCode != 0) {
                return null;
            }

            // export in xml formats --------------------------------
            String omrPath = workingDir + "\\Out" + path.substring(path.lastIndexOf('\\'), path.lastIndexOf('.')) + ".omr";
            String commandXML = audiverisPath + " -batch -export -option " + options[0] +" -output " + outputDir + " -- " + omrPath;
            System.out.println("Audiveris commandXML: " + commandXML);

            System.out.println("Running Audiveris..");
            ProcessBuilder processBuilderXML = new ProcessBuilder("cmd.exe", "/c", commandXML);
            Process processXML = processBuilderXML.start();

            // Read the output of the command
            BufferedReader readerXML = new BufferedReader(new InputStreamReader(processXML.getInputStream()));
            String lineXML;
            while ((lineXML = readerXML.readLine()) != null) {
                System.out.println(lineXML);
            }
            int exitCodeXML = processXML.waitFor();

            if (exitCodeXML != 0) {
                return null;
            }
            //--------------------------------------------------------

        } catch (Exception e) {
            System.out.println("Error running Audiveris: " + e.getMessage());
            return null;
        }

        String mxlPath = workingDir + "\\Out" + path.substring(path.lastIndexOf('\\'), path.lastIndexOf('.')) + ".mxl";
        System.out.println("MXL Path: " + mxlPath);

        // Checking if file got converted
        if (!new File(mxlPath).exists()) {
            System.out.println("MXL file not found.");
            return null;
        }

        // Convert .mxl to .mid
        String midiPath = convertMxlToMidi(mxlPath);
        System.out.println("MIDI Path: " + midiPath);

        // Checking if .mid file got converted
        if (!new File(midiPath).exists()) {
            System.out.println("MIDI file not found.");
            return null;
        }

        // Start a new thread for cleaning up the directories after a delay
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
                cleanupDirectories(workingDir + "\\In", workingDir + "\\Out");
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting to clean up directories: " + e.getMessage());
            }
        }).start();

        // Return the .mid path
        return midiPath;
    }

    private String convertMxlToMidi(String mxlPath) {
        String pythonScriptPath = System.getProperty("user.dir") + "/src/main/MxlToMidi.py";
        String command = "python " + pythonScriptPath + " " + mxlPath;

        System.out.println("Python command: " + command);

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

    private void cleanupDirectories(String inputDir, String outputDir) {
        deleteFilesInDirectory(inputDir);
        deleteFilesInDirectory(outputDir);
    }

    private void deleteFilesInDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }
}
