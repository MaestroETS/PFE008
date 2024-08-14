package PFE008.backend;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
    
    /**
     * Save a file to a directory, with a random code as the file name
     * 
     * @param fileExtension The extension of the file
     * @param multipartFile The file to save
     * @param dir The directory to save the file to
     * @return The path to the saved file
     * @throws IOException  If the file could not be saved
     */
    public static Path saveFile(String fileExtension, MultipartFile multipartFile, String dir) throws IOException {
        Path uploadPath = Paths.get(dir);

        if (Files.notExists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
 
        String fileCode = RandomStringUtils.randomAlphanumeric(8);
        Path filePath = uploadPath.resolve(fileCode + fileExtension);
         
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {       
            throw new IOException("Could not save file: " + fileCode + fileExtension, ioe);
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
        Path foundFile = Path.of(fileCode);
 
        if (Files.exists(foundFile)) {
            return new UrlResource(foundFile.toUri());
        }
         
        throw new IOException("File not found: " + fileCode);
    }

    /**
     * Validate the file type using Apache Tika
     * 
     * @param multipartFile The file to validate
     * @return true if the file is a valid type, false otherwise
     * @throws IOException If an error occurs while reading the file
     */
    public static boolean isValidFile(MultipartFile multipartFile) throws IOException {
        Tika tika = new Tika();
        try (InputStream input = multipartFile.getInputStream()) {
            String mimeType = tika.detect(input);
            return mimeType.equals("application/pdf") || mimeType.equals("image/jpeg") || mimeType.equals("image/png");
        }
    }

    /**
     * Cleanup files in the specified directories that start with the given prefix
     * 
     * @param inputDir The input directory
     * @param outputDir The output directory
     * @param fileNamePrefix The prefix of the files to delete
     */
    public static void cleanupDirectories(String inputDir, String outputDir, String fileNamePrefix) {
        deleteFilesInDirectory(inputDir, fileNamePrefix);
        deleteFilesInDirectory(outputDir, fileNamePrefix);
    }

    /**
     * Delete files in a directory that start with a specific prefix
     * 
     * @param directoryPath The path of the directory
     * @param fileNamePrefix The prefix of the files to delete
     */
    private static void deleteFilesInDirectory(String directoryPath, String fileNamePrefix) {
        Path dirPath = Paths.get(directoryPath);
        if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
            try {
                Files.list(dirPath)
                        .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().startsWith(fileNamePrefix))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                System.err.println("Failed to delete file: " + path + " - " + e.getMessage());
                            }
                        });
            } catch (IOException e) {
                System.err.println("Failed to list files in directory: " + dirPath + " - " + e.getMessage());
            }
        }
    }
}
