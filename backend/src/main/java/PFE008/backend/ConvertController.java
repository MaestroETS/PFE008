package PFE008.backend;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

	private final AtomicLong counter = new AtomicLong();

	@GetMapping("/convert")
	public Conversion convert(@RequestParam(value = "path", defaultValue = "") String path) {
		
		// Check if path is empty
		if (path.isEmpty()) {
			return new Conversion(counter.incrementAndGet(), "No path specified!");
		}

		// Check if path is valid and file exists
		if (!new File(path).exists()) {
			return new Conversion(counter.incrementAndGet(), "File does not exist!");
		}

		// Convert music sheet to .mxl
		AudiverisController audiveris = new AudiverisController();
		String mxlPath = audiveris.convert(path);

		// Return error if audiveris controller hasn't converted file
		if(mxlPath == null) {
			return new Conversion(counter.incrementAndGet(), "Error converting the file.");
		}

		System.out.println(".MXL Path : " + mxlPath);

		return new Conversion(counter.incrementAndGet(), "File converted. The .mxl Path is : " + mxlPath);
	}
}
