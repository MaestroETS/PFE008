package PFE008.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AudiverisController {
    private String tempos;
    private String terminalType;
    private String terminalOption;
    private String audiverisPath;
    private String outputDir;

    public AudiverisController() {
        this(null);
    }

    public AudiverisController(String tempos) {
        this.tempos = tempos;
        initializePathsAndOptions();
    }

    private void initializePathsAndOptions() {
        String workingDir = System.getProperty("user.dir");
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            terminalType = "cmd.exe";
            terminalOption = "/c";
            audiverisPath = workingDir + File.separator + "audiveris" + File.separator + "dist" + File.separator + "bin" + File.separator + "Audiveris.bat";
        } else if (os.contains("linux") || os.contains("mac")) {
            terminalType = "sh";
            terminalOption = "-c";
            audiverisPath = workingDir + File.separator + "backend" + File.separator + "Audiveris" + File.separator + "dist" + File.separator + "bin" + File.separator + "Audiveris";
        }

        outputDir = workingDir + File.separator + "Out";
    }

    public String convert(String path) {
        String workingDir = System.getProperty("user.dir");
        String inputFile = workingDir + File.separator + path;
        String[] options = {"org.audiveris.omr.sheet.BookManager.useCompression=false"};

        // Run the command to process the music sheet
        String commandMXL = audiverisPath + " -batch -export -output " + outputDir + " -- " + inputFile;
        if (!runCommand(commandMXL)) {
            return null;
        }

        // Export in XML format
        String omrPath = outputDir + File.separator + new File(path).getName().replaceAll("\\..*$", "") + ".omr";
        String commandXML = audiverisPath + " -batch -export -option " + options[0] + " -output " + outputDir + " -- " + omrPath;
        if (!runCommand(commandXML)) {
            return null;
        }

        // Convert .mxl to .mid
        String mxlPath = outputDir + File.separator + new File(path).getName().replaceAll("\\..*$", "") + ".mxl";
        if (!new File(mxlPath).exists()) {
            System.out.println("MXL file not found.");
            return null;
        }

        String midiPath = convertMxlToMidi(mxlPath);
        return midiPath != null && new File(midiPath).exists() ? midiPath : null;
    }

    private boolean runCommand(String command) {
        System.out.println("Running command: " + command);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(terminalType, terminalOption, command);
            Process process = processBuilder.start();

            Future<?> outputHandler = Executors.newSingleThreadExecutor().submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            int exitCode = process.waitFor();
            outputHandler.get(); // Wait for output handler to finish
            System.out.println("Process exited with code: " + exitCode);

            return exitCode == 0;
        } catch (Exception e) {
            System.out.println("Error running command: " + e.getMessage());
            return false;
        }
    }

    private String convertMxlToMidi(String mxlPath) {
        String pythonScriptPath = System.getProperty("user.dir") + File.separator + "backend" + File.separator + "src" + File.separator + "main" + File.separator + "MxlToMidi.py";

        String command = "python " + pythonScriptPath + " \"" + mxlPath + "\"";
        if (tempos != null && !tempos.isEmpty()) {
            command += " \"" + tempos.replace("\"", "\\\"") + "\"";
        }

        if (!runCommand(command)) {
            return null;
        }

        return mxlPath.replace(".mxl", ".mid");
    }
}
