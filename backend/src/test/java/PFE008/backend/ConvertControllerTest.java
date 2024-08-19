package PFE008.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

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
 * ConvertControllerTest
 *
 * This class contains unit tests for the ConvertController.
 * The tests cover both successful and error scenarios for file conversion.
 * 
 * @author Lafleche Chevrette
 * @version 2024.07.08
 */
@SpringBootTest
public class ConvertControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private static final String TEST_PATH = System.getProperty("user.dir") + File.separator 
            + "src" + File.separator + "test" + File.separator + "java" + File.separator 
            + "PFE008" + File.separator + "backend" + File.separator 
            + "resources" + File.separator + "tests_java" + File.separator;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testConvertSuccess() throws Exception {
        Path path = Paths.get(TEST_PATH + "AudiverisController_java_tests.pdf");
        byte[] file = Files.readAllBytes(path);

        MockMultipartFile multipartFile = new MockMultipartFile("file", "AudiverisController_java_tests.pdf", "application/pdf", file);

        mockMvc.perform(multipart("/convert")
                .file(multipartFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"output.mxl\""))
                .andExpect(content().contentType("application/octet-stream"));
    }

    @Test
    public void testConvertFail() throws Exception {
        Path path = Paths.get(TEST_PATH + "wrong.pdf");
        byte[] file = Files.readAllBytes(path);

        MockMultipartFile multipartFile = new MockMultipartFile("file", "wrong.pdf", "application/pdf", file);

        mockMvc.perform(multipart("/convert")
                .file(multipartFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Could not convert file"));
    }

    @Test
    public void testConvertFileNotFound() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());

        mockMvc.perform(multipart("/convert")
                .file(multipartFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Could not convert file"));
    }

    @Test
    public void testConvertWrongFormat() throws Exception {
        Path path = Paths.get(TEST_PATH + "imageTest.jpg");
        byte[] file = Files.readAllBytes(path);

        MockMultipartFile multipartFile = new MockMultipartFile("file", "imageTest.jpg", "image/jpeg", file);

        mockMvc.perform(multipart("/convert")
                .file(multipartFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File is invalid. Must be a pdf, jpg, jpeg or png"));
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

        MockMultipartFile multipartFile = new MockMultipartFile("file", "malicious.pdf", "application/pdf", maliciousPdf.getBytes());

        mockMvc.perform(multipart("/convert")
                .file(multipartFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Could not convert file"));
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
                            System.err.println("Failed to delete: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }
}

/**
 * Health Check Test
 * 
 * This class tests the /health endpoint to verify that the service is running correctly.
 * 
 * @version 2024.08.19
 */
@SpringBootTest
public class HealthCheckTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testHealthCheck() throws Exception {
        mockMvc.perform(get("/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Service is up and running"));
    }
}
