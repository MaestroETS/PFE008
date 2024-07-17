package PFE008.backend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        String path = OUTPATH + File.separator + "AudiverisController_java_tests.mid";
        ArrayList<Integer> notesToTests = new ArrayList<>();
        int[] goodNotes = {72, 71, 69, 67, 67, 69, 71, 72, 72, 71, 69, 67, 67, 69, 71, 72, 55, 53, 52, 50, 50, 52, 53, 55, 55, 53, 52, 50, 50, 52, 53, 55};

        try {
            Sequence sequence = MidiSystem.getSequence(new File(path));
            Track[] tracks = sequence.getTracks();

            for (Track track : tracks) {
                for (int i = 0; i < track.size(); i++) {
                    MidiEvent event = track.get(i);
                    MidiMessage message = event.getMessage();

                        if (message instanceof ShortMessage) {
                            ShortMessage sm = (ShortMessage) message;
                            if (sm.getCommand() == ShortMessage.NOTE_ON) {
                                int note = sm.getData1();
                                notesToTests.add(note);
                                
                                long durationTicks = 0;
                                for (int j = i + 1; j < track.size(); j++) {
                                    MidiEvent offEvent = track.get(j);
                                    MidiMessage offMessage = offEvent.getMessage();
                                    if (offMessage instanceof ShortMessage offSM) {
                                        if (offSM.getCommand() == ShortMessage.NOTE_OFF && offSM.getData1() == note) {
                                            durationTicks = offEvent.getTick() - event.getTick();
                                            break;
                                        }
                                    }
                                }
    
                                double durationQuarterNotes = (double) durationTicks / sequence.getResolution();
                                assertTrue(Math.abs(durationQuarterNotes - 1.0) < 0.01);
                            }
                        }
                }
            }
        } catch (IOException | ArrayIndexOutOfBoundsException | InvalidMidiDataException e) {
        }

        for(int i =0; i <  goodNotes.length; i++) {
            assertEquals(notesToTests.get(i), goodNotes[i]);
        }
    }

    @AfterAll
    static void cleanup() {

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