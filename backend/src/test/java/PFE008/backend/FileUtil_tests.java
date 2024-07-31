package PFE008.backend;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileUtil_tests {

    private static String TESTPATH = System.getProperty("user.dir") + File.separator 
    + "src" + File.separator + "test" + File.separator + "java" + File.separator + "PFE008" + File.separator 
    + "backend" + File.separator + "resources" + File.separator + "tests_java" + File.separator + "testDir";

    @BeforeAll
    static public void setup() throws IOException {
        Files.createDirectories(Paths.get(TESTPATH));
    }

    @Test
    public void testSaveFile() throws Exception {
        String fileName = "test.pdf";

        MockMultipartFile multipartFile = new MockMultipartFile("file", fileName, "application/pdf", "Test Data".getBytes());

        Path savedFilePath = FileUtil.saveFile(fileName, multipartFile, TESTPATH);

        assertTrue(Files.exists(savedFilePath));
        Files.deleteIfExists(savedFilePath);
    }

    @Test
    public void testGetFileAsResource() throws Exception {
        String fileName = "test.pdf";
        Path filePath = Paths.get(TESTPATH, "abcd1234-" + fileName);

        Files.write(filePath, "Test Data".getBytes());

        Resource resource = new FileUtil().getFileAsResource(filePath.toString());

        assertNotNull(resource, "La ressource ne doit pas être nulle");
        assertTrue(resource.exists(), "La ressource doit exister");
        assertEquals(filePath.toUri(), resource.getURI(), "L'URI de la ressource doit correspondre à l'URI du fichier");
    }

    @Test
    public void testSaveFileThrowsIOException() throws IOException {

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getInputStream()).thenThrow(new IOException("Test"));

        IOException exception = assertThrows(IOException.class, () -> {
            FileUtil.saveFile("test.pdf", multipartFile, TESTPATH);
        });
        assertEquals("Could not save file: test.pdf", exception.getMessage());
    } 

    @Test
    public void testSaveFileThrowsIOExceptionOnCopy() throws IOException {

        MultipartFile multipartFile = mock(MultipartFile.class);
        InputStream inputStream = mock(InputStream.class);

        when(multipartFile.getInputStream()).thenReturn(inputStream);
        doThrow(new IOException("Test")).when(inputStream).close();

        try {
            FileUtil.saveFile("test.pdf", multipartFile, TESTPATH);
            fail("Expected IOException not thrown");
        } catch (IOException e) {
            assertEquals("Could not save file: test.pdf", e.getMessage());
        }

        assertFalse(Files.exists(Paths.get(TESTPATH).resolve("test.pdf")));
    
    }


    @AfterAll
    static public void cleanup() throws IOException {

        File directory = new File(TESTPATH);

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

        Files.deleteIfExists(Paths.get(TESTPATH));
    }
}