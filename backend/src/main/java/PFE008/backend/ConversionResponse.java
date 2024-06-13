package PFE008.backend;

/**
 * Conversion class
 * 
 * This class will be used to return the result of the conversion
 * to the frontend.
 * 
 * @author Charlie Poncsak
 * @version 2024.06.06
 */
public class ConversionResponse {
    private String fileName;
    private String downloadUri;
    private long size;
 
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDownloadUri() {
        return downloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        this.downloadUri = downloadUri;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}