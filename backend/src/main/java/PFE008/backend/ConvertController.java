package PFE008.backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * ConvertController class
 * 
 * This class will be used to handle the conversion of music sheets to MIDI files.
 * The controller will take a path to a music sheet file as input.
 * It will then convert the file to a .mxl file and
 * return its path.
 * 
 * MIDI conversion is to be implemented.
 * 
 * @author Charlie Poncsak
 * @version 2024.06.06
 */
@RestController
public class ConvertController {

	@PostMapping("/convert")
    @CrossOrigin(origins = "*")
	public ResponseEntity<?> convert(@RequestParam("file") MultipartFile multipartFile) throws IOException {
		FileUtil downloadUtil = new FileUtil();
		
		// Save input file
		String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        Path filePath = FileUtil.saveFile(fileName, multipartFile, "In");
		
		// Convert music sheet to .mxl
		AudiverisController audiveris = new AudiverisController();
		String mxlPath = audiveris.convert(filePath.toString());

		if (mxlPath == null) {
			return new ResponseEntity<>("Could not convert file", HttpStatus.INTERNAL_SERVER_ERROR);
		}
         
		//Build response
        Resource resource = null;
        try {
            resource = downloadUtil.getFileAsResource(mxlPath);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
         
        if (resource == null) {
            return new ResponseEntity<>("Could not find converted file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
         
        String contentType = "application/octet-stream";
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
         
        return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(contentType))
			.header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
			.body(resource); 
	}
}
