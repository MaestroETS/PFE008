package PFE008.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AudiverisController {
    private String tempos;

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

        String audiverisPath = workingDir + "/audiveris/dist/bin/Audiveris.bat";
        String inputFile = "\"" + path + "\"";
        String outputDir = workingDir + "\\Out";
        String XMLoption = "org.audiveris.omr.sheet.BookManager.useCompression=false";
        String[] options = new String[]{"org.audiveris.omr.sheet.BookManager.ProcessingSwitches.chordNames=true", "org.audiveris.omr.sheet.BookManager.ProcessingSwitches.lyrics=true", "org.audiveris.omr.sheet.BookManager.ProcessingSwitches.lyricsAboveStaff=false"};
        String commandMXL = audiverisPath + " -batch -export"+ ((options.length > 0) ? " -option " + stringifyOptions(options) : "") +" -output " + outputDir + " -- " + inputFile;

        System.out.println("Audiveris commandMXL: " + commandMXL);

        // Run the command
        try {
            System.out.println("Running Audiveris..");
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", commandMXL);
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
            String omrPath = workingDir + "\\Out" + path.substring(path.lastIndexOf('\\'), path.lastIndexOf('.')) + ".omr";
            String commandXML = audiverisPath + " -batch -export -option " + XMLoption +
                    ((options.length > 0) ? " -option " + stringifyOptions(options) : "") +
                    " -output " + outputDir + " -- " + omrPath;
            System.out.println("Audiveris commandXML: " + commandXML);

            System.out.println("Running Audiveris..");
            ProcessBuilder processBuilderXML = new ProcessBuilder("cmd.exe", "/c", commandXML);
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

        String mxlPath = workingDir + "\\Out" + path.substring(path.lastIndexOf('\\'), path.lastIndexOf('.')) + ".mxl";
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
                cleanupDirectories(workingDir + "\\In", workingDir + "\\Out");
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting to clean up directories: " + e.getMessage());
            }
        }).start();

        return midiPath;
    }

    private String convertMxlToMidi(String mxlPath) {
        String pythonScriptPath = System.getProperty("user.dir") + "/src/main/MxlToMidi.py";
        String command = "python " + pythonScriptPath + " " + mxlPath;

        if (tempos != null && !tempos.isEmpty()) {
            command += " \"" + tempos.replace("\"", "\\\"") + "\"";
        }

        System.out.println("Python command: " + command);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
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

    private String stringifyOptions(String[] options) {
        return Arrays.stream(options)
                .collect(Collectors.joining(" -option "));
    }
}
