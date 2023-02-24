package millimeeter.server.controller;

import java.util.Map;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import millimeeter.server.model.Match;
import millimeeter.server.model.Swipe;
import millimeeter.server.service.SwipeService;
import millimeeter.server.service.assembler.MatchModelAssembler;
import millimeeter.server.service.assembler.SwipeModelAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Validated
@RequestMapping("/api/v1/swipes")
public class SwipeController {

  private final SwipeService swipeService;
  private final MatchModelAssembler matchModelAssembler;
  private final SwipeModelAssembler swipeModelAssembler;

  @Autowired
  public SwipeController(
      SwipeService swipeService,
      MatchModelAssembler matchModelAssembler,
      SwipeModelAssembler swipeModelAssembler) {
    this.swipeService = swipeService;
    this.matchModelAssembler = matchModelAssembler;
    this.swipeModelAssembler = swipeModelAssembler;
  }

  @PostMapping("/{id}/{direction}")
  public ResponseEntity<Object> swipe(
      @PathVariable @Positive(message = "Profile id must be a positive number") Long id,
      @PathVariable
          @Pattern(regexp = "LEFT|RIGHT", message = "The swipe value must be LEFT or RIGHT")
          String direction) {
    Map<String, Object> resultMap = swipeService.swipe(id, direction);
    if (resultMap.containsKey("match")) {
      return new ResponseEntity<>(
          matchModelAssembler.toModel((Match) resultMap.get("match")), HttpStatus.CREATED);
    } else {
      return new ResponseEntity<>(
          swipeModelAssembler.toModel((Swipe) resultMap.get("swipe")), HttpStatus.CREATED);
    }
  }
}
