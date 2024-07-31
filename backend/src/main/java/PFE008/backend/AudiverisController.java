package PFE008.backend;

import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

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

        String audiverisPath = workingDir + File.separator + "backend" + File.separator + "Audiveris" + File.separator + "dist" + File.separator + "bin" + File.separator + "Audiveris";
        String inputFile = workingDir + File.separator + path + File.separator;
        String outputDir = workingDir + File.separator + "Out";
        String[] options = new String[]{"org.audiveris.omr.sheet.BookManager.useCompression=false"};


        // fix for docker container running on linux
        // Check the operating system
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            System.out.println("Windows OS detected");
            audiverisPath = workingDir + File.separator + "audiveris" + File.separator + "dist" + File.separator + "bin" + File.separator + "Audiveris.bat";
            terminalType = "cmd.exe";
            terminalOption = "/c";
        } else if (os.contains("linux") || os.contains("mac")) {
            System.out.println("Linux or macOS OS detected");
            audiverisPath = workingDir + File.separator + "backend" + File.separator + "Audiveris" + File.separator + "dist" + File.separator + "bin" + File.separator + "Audiveris";
            terminalType = "sh";
            terminalOption = "-c";
        }

        String commandMXL = audiverisPath + " -batch -export -output " + outputDir + " -- " + inputFile;
        System.out.println("Audiveris commandMXL: " + commandMXL);

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
            System.out.println("Exporting to XML..");
            System.out.println("omrPath..");
            String omrPath = workingDir + File.separator + "Out" + path.substring(path.lastIndexOf(File.separator), path.lastIndexOf('.')) + ".omr";
            System.out.println("commandXML..");
            String commandXML = audiverisPath + " -batch -export -option " + options[0] +" -output " + outputDir + " -- " + omrPath;
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

        String mxlPath = workingDir + File.separator + "Out" + path.substring(path.lastIndexOf(File.separator), path.lastIndexOf('.')) + ".mxl";
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
      
        return midiPath;
    }

    private String convertMxlToMidi(String mxlPath) {
        String pythonScriptPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "MxlToMidi.py";

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            pythonScriptPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "MxlToMidi.py";
        }
        if (os.contains("linux") || os.contains("mac")) {
            pythonScriptPath = System.getProperty("user.dir") + File.separator + "backend" + File.separator + "src" + File.separator + "main" + File.separator + "MxlToMidi.py";
        }

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
}
