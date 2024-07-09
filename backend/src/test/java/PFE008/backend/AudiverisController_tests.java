package PFE008.backend;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



/**
 * Test Controller for Audiveris
 * 
 * 
 * @author Lafleche Chevrette
 * @version 2024.07.08
 */

class AudiverisController_tests {
    private AudiverisController audiverisController;

    // src/test/java/PFE008/backend/resources/tests_java/
    private final String TESTPATH = System.getProperty("user.dir") + File.separator 
    + "src" + File.separator + "test" + File.separator + "java" + File.separator + "PFE008" + File.separator 
    + "backend" + File.separator + "resources" + File.separator + "tests_java" + File.separator;
    
    private final String TESTFILE = "AudiverisController_java_tests.pdf";

    private static final String OUTPATH = System.getProperty("user.dir") + File.separator + "Out" + File.separator;

    @BeforeEach
    void setup() {
        audiverisController = new AudiverisController();
    }

    @Test
    void testConvertSuccess() throws Exception {

        String path = TESTPATH + TESTFILE;

        String midiPath = audiverisController.convert(path);
        assertNotNull(midiPath); // The path exists
        assertTrue(midiPath.endsWith(".mid")); // A MIDI file was created
        File file = new File(midiPath);
        assertTrue(file.exists()); // The file exists
        assertTrue(file.length() > 0); // The file is not empty
        
    }


    @Test
    void testConvertWrongFile() throws Exception {
        String path = TESTPATH + "wrong.pdf";
        String midiPath = audiverisController.convert(path);
        assertNull(midiPath);
    }

    @Test
    void testConvertFileOutput() throws Exception {
        String path = TESTPATH + OUTPATH + File.separator + "AudiverisController_java_tests.mxl";
        File file = new File(path);

        // In progress
            

        assertTrue(true);
    }

    //@AfterAll
    static void deleteFiles() {

        File directory = new File(OUTPATH);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        if (!file.delete()) {
                            System.err.println("Impossible to delete : " + file.getAbsolutePath());
                        }
                    }else{
                        System.err.println(file.getAbsolutePath() + " is not a file or does not exists");
                    }
                }
            } else {
                System.err.println("No files were found in " + directory.getAbsolutePath());
            }
        } else {
            System.err.println(directory.getAbsolutePath() + " is not a directory or does not exists");
        }

    }


}