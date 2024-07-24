package PFE008.backend;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * FileUtil class
 * 
 * This class will be used to handle file operations.
 * 
 * @author Charlie Poncsak
 * @version 2024.06.13
 */
public class FileUtil {
    private Path foundFile;
    
    /**
     * Save a file to a directory, and add a random code to the file name
     * 
     * @param fileName The name of the file
     * @param multipartFile The file to save
     * @param dir The directory to save the file to
     * @return The path to the saved file
     * @throws IOException  If the file could not be saved
     */
    public static Path saveFile(String fileName, MultipartFile multipartFile, String dir) throws IOException {
        Path uploadPath = Paths.get(dir);
        Path filePath;
          
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
 
        String fileCode = RandomStringUtils.randomAlphanumeric(8);
         
        try (InputStream inputStream = multipartFile.getInputStream()) {
            filePath = uploadPath.resolve(fileCode + "-" + fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {       
            throw new IOException("Could not save file: " + fileName, ioe);
        }
         
        return filePath;
    }

    /**
     * Get a file as a Resource
     * 
     * @param fileCode The code of the file
     * @return The file as a Resource
     * @throws IOException If the file could not be found
     */
    public Resource getFileAsResource(String fileCode) throws IOException {
        foundFile = Path.of(fileCode);
 
        if (foundFile != null) {
            return new UrlResource(foundFile.toUri());
        }
         
        return null;
    }

    public static boolean isValidFile(MultipartFile multipartFile) throws IOException {
        Tika tika = new Tika();
        try (InputStream input = multipartFile.getInputStream()) {
            String mimeType = tika.detect(input);
            return mimeType.equals("application/pdf") || mimeType.equals("image/jpeg") || mimeType.equals("image/jpg") || mimeType.equals("image/png");
        }
    }
}
