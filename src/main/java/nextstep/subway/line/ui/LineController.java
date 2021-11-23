package nextstep.subway.line.ui;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nextstep.subway.line.application.LineService;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;

@RestController
@RequestMapping("/lines")
public class LineController {
	private final LineService lineService;

	public LineController(final LineService lineService) {
		this.lineService = lineService;
	}

	@PostMapping
	public ResponseEntity<LineResponse> createLine(@RequestBody LineRequest lineRequest) {
		LineResponse line = lineService.saveLine(lineRequest);
		return ResponseEntity.created(URI.create("/lines/" + line.getId())).body(line);
	}

	@GetMapping
	public ResponseEntity<List<LineResponse>> getLines() {
		List<LineResponse> lines = lineService.getLines();
		return ResponseEntity.ok(lines);
	}

	@GetMapping("/{id}")
	public ResponseEntity<LineResponse> getLineById(@PathVariable("id") Long id) {
		return ResponseEntity.ok(lineService.getLineById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<LineResponse> modifyLine(@PathVariable("id") Long id, @RequestBody LineRequest lineRequest) {
		return ResponseEntity.ok(lineService.modify(id, lineRequest));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<LineResponse> deleteLine(@PathVariable("id") Long id) {
		lineService.deleteLineById(id);
		return ResponseEntity.noContent().build();
	}

}
