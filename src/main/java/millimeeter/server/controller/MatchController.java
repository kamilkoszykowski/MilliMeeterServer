package millimeeter.server.controller;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.Positive;
import millimeeter.server.dto.MatchDto;
import millimeeter.server.dto.MatchWithMessagesDto;
import millimeeter.server.service.MatchService;
import millimeeter.server.service.assembler.MatchDtoModelAssembler;
import millimeeter.server.service.assembler.MatchModelAssembler;
import millimeeter.server.service.assembler.MatchWithMessagesDtoModelAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Validated
@RequestMapping("/api/v1")
public class MatchController {

  private final MatchService matchService;
  private final MatchModelAssembler matchModelAssembler;
  private final MatchDtoModelAssembler matchDtoModelAssembler;
  private final MatchWithMessagesDtoModelAssembler matchWithMessagesDtoModelAssembler;

  @Autowired
  public MatchController(
      MatchService matchService,
      MatchModelAssembler matchModelAssembler,
      MatchDtoModelAssembler matchDtoModelAssembler,
      MatchWithMessagesDtoModelAssembler matchWithMessagesDtoModelAssembler) {
    this.matchService = matchService;
    this.matchModelAssembler = matchModelAssembler;
    this.matchDtoModelAssembler = matchDtoModelAssembler;
    this.matchWithMessagesDtoModelAssembler = matchWithMessagesDtoModelAssembler;
  }

  @GetMapping("/matches")
  public ResponseEntity<CollectionModel<EntityModel<MatchDto>>> findMatches() {
    List<EntityModel<MatchDto>> matchesModel =
        matchService.findAllMatches().stream()
            .map(matchDtoModelAssembler::toModel)
            .collect(Collectors.toList());
    return new ResponseEntity<>(CollectionModel.of(matchesModel), HttpStatus.OK);
  }

  @GetMapping("/conversations")
  public ResponseEntity<CollectionModel<EntityModel<MatchWithMessagesDto>>>
      findMatchesWithMessages() {
    List<EntityModel<MatchWithMessagesDto>> matchesModel =
        matchService.findAllMatchesWithMessages().stream()
            .map(matchWithMessagesDtoModelAssembler::toModel)
            .collect(Collectors.toList());
    return new ResponseEntity<>(CollectionModel.of(matchesModel), HttpStatus.OK);
  }

  @DeleteMapping("/matches/{id}")
  public ResponseEntity<Void> deleteMatch(
      @PathVariable @Positive(message = "The match id must be a positive number") Long id) {
    matchService.deleteMatchById(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
