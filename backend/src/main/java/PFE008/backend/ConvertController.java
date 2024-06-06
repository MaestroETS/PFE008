package PFE008.backend;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConvertController {

	private static final String template = "File %s converted!";
	private final AtomicLong counter = new AtomicLong();

	@GetMapping("/convert")
	public Convert convert(@RequestParam(value = "name", defaultValue = "not found and not") String name) {
		return new Convert(counter.incrementAndGet(), String.format(template, name));
	}
}
