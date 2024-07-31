package PFE008.backend;

import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class AudiverisController {
    private String tempos;
    
    private String terminalType = "cmd.exe";
    private String terminalOption = "/c";

    public AudiverisController() {
        this(null);
    }

    // Constructor with tempos parameter
    public AudiverisController(String tempos) {
        this.tempos = tempos;
    }

    /**
     * Convert a music sheet to a .mid file
     * 
     * @param path Path to the music sheet file
     * @return The .mid path if it has been created, null otherwise
     */
    public String convert(String path) {
        String workingDir = System.getProperty("user.dir");
        System.out.println("Working Directory: " + workingDir);

        String audiverisPath = workingDir + "/audiveris/dist/bin/Audiveris";
        String inputFile = "/" + path + "/";
        String outputDir = workingDir + "/Out";
        String[] options = new String[]{"org.audiveris.omr.sheet.BookManager.useCompression=false"};
        String commandMXL = audiverisPath + " -batch -export -output " + outputDir + " -- " + inputFile;

        System.out.println("Audiveris commandMXL: " + commandMXL);

        // fix for docker container running on linux
        String os = SystemUtils.OS_NAME;
        // check if os contain the word windows or linux
        if (os.toLowerCase().contains("windows")) {
            System.out.println("Windows OS detected");
            terminalType = "cmd.exe";
            terminalOption = "/c";
        } else if (os.toLowerCase().contains("linux")) {
            System.out.println("Linux OS detected");
            terminalType = "sh";
            terminalOption = "-c";
        }

        // Run the command
        try {
            System.out.println("Running Audiveris..");
            ProcessBuilder processBuilder = new ProcessBuilder(terminalType, terminalOption, commandMXL);
            Process process = processBuilder.start();

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

            // Export in XML formats
            String omrPath = workingDir + "/Out" + path.substring(path.lastIndexOf('\\'), path.lastIndexOf('.')) + ".omr";
            String commandXML = audiverisPath + " -batch -export -option " + options[0] +" -output " + outputDir + " -- " + "/" + omrPath + "/";
            System.out.println("Audiveris commandXML: " + commandXML);

            System.out.println("Running Audiveris..");
            ProcessBuilder processBuilderXML = new ProcessBuilder(terminalType, terminalOption, commandXML);
            Process processXML = processBuilderXML.start();

            BufferedReader readerXML = new BufferedReader(new InputStreamReader(processXML.getInputStream()));
            String lineXML;
            while ((lineXML = readerXML.readLine()) != null) {
                System.out.println(lineXML);
            }
            int exitCodeXML = processXML.waitFor();

            if (exitCodeXML != 0) {
                return null;
            }

        } catch (Exception e) {
            System.out.println("Error running Audiveris: " + e.getMessage());
            return null;
        }

        String mxlPath = workingDir + "/Out" + path.substring(path.lastIndexOf('\\'), path.lastIndexOf('.')) + ".mxl";
        System.out.println("MXL Path: " + mxlPath);

        if (!new File(mxlPath).exists()) {
            System.out.println("MXL file not found.");
            return null;
        }

        // Convert .mxl to .mid
        String midiPath = convertMxlToMidi(mxlPath);
        System.out.println("MIDI Path: " + midiPath);

        if (!new File(midiPath).exists()) {
            System.out.println("MIDI file not found.");
            return null;
        }

        // Start a new thread for cleaning up the directories after a delay
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
                cleanupDirectories(workingDir + "/In", workingDir + "/Out");
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting to clean up directories: " + e.getMessage());
            }
        }).start();

        return midiPath;
    }

    private String convertMxlToMidi(String mxlPath) {
        String pythonScriptPath = System.getProperty("user.dir") + "/src/main/MxlToMidi.py";
        String command = "python " + pythonScriptPath + " " + "\"" + mxlPath + "\"";

        if (tempos != null && !tempos.isEmpty()) {
            command += " \"" + tempos.replace("\"", "\\\"") + "\"";
        }

        System.out.println("Python command: " + command);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(terminalType, terminalOption, command);
            Process process = processBuilder.start();

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
