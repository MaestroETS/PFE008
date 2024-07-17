package PFE008.backend;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


/**
 * Test Controller for Audiveris
 * 
 * 
 * @author Lafleche Chevrette
 * @version 2024.07.08
 */

@SpringBootTest
public class ConvertControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private static String TESTPATH = System.getProperty("user.dir") + File.separator 
    + "src" + File.separator + "test" + File.separator + "java" + File.separator + "PFE008" + File.separator 
    + "backend" + File.separator + "resources" + File.separator + "tests_java" + File.separator;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testConvertSuccess() throws Exception {

    Path path = Paths.get(TESTPATH + "AudiverisController_java_tests.pdf");
    byte[] file = Files.readAllBytes(path);

    MockMultipartFile multipartFile = new MockMultipartFile("file", "test.pdf", "application/pdf", file);

    mockMvc.perform(multipart("/convert")
            .file(multipartFile)
            .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk());
        
    }

    @Test
    public void testConvertFail() throws Exception {

    Path path = Paths.get(TESTPATH + "wrong.pdf");
    byte[] file = Files.readAllBytes(path);

    MockMultipartFile multipartFile = new MockMultipartFile("file", "test.pdf", "application/pdf", file);

    mockMvc.perform(multipart("/convert")
            .file(multipartFile)
            .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isInternalServerError());
        
    }

    @Test
    public void testConvertFileNotFound() throws Exception {

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());

        mockMvc.perform(multipart("/convert")
                .file(multipartFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testConvertWrongFormat() throws Exception {

        Path path = Paths.get(TESTPATH + "imageTest.jpg");
        byte[] file = Files.readAllBytes(path);

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.pdf", "application/pdf", file);

        mockMvc.perform(multipart("/convert")
                .file(multipartFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError());
    }


    @Test
    public void testConvertMaliciousFile() throws Exception {

        String maliciousPdf = """
                                     %PDF-1.4
                                     1 0 obj
                                     << /Type /Catalog >>
                                     endobj
                                     xref
                                     0 1
                                     0000000000 65535 f 
                                     trailer
                                     << /Root 1 0 R >>
                                     %%EOF
                                     MALICIOUS DATA"""; 

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.pdf", "application/pdf", maliciousPdf.getBytes());

        mockMvc.perform(multipart("/convert")
                .file(multipartFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError());
    }


    @AfterAll
    static void cleanup() {

        File directoryOut = new File(System.getProperty("user.dir") + File.separator + "Out" + File.separator);
        File directoryIn = new File(System.getProperty("user.dir") + File.separator + "In" + File.separator);

        deleteFiles(directoryOut);
        deleteFiles(directoryIn);

    }

    private static void deleteFiles(File directory) {
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
