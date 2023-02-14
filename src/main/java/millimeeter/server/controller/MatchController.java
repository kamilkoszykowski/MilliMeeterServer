package millimeeter.server.controller;

import static millimeeter.server.controller.response.MatchControllerResponses.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.constraints.Positive;
import millimeeter.server.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Validated
@Tag(name = "Match")
@RequestMapping("/api/v1")
public class MatchController {

  @Autowired private MatchService matchService;

  @Operation(
      summary = "Get matches without messages",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = "application/hal+json",
            examples = {
              @ExampleObject(
                  name = "No matches found",
                  value = FIND_MATCHES_OK_NO_MATCHES_RESPONSE),
              @ExampleObject(name = "Matches found", value = FIND_MATCHES_OK_WITH_MATCHES_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "422",
      description = "UNPROCESSABLE ENTITY",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(
                  name = "Profile not exists",
                  value = FIND_MATCHES_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            })
      })
  @GetMapping("/matches")
  public ResponseEntity<Object> findMatches() {
    return matchService.findAllMatches();
  }

  @Operation(
      summary = "Get matches with messages",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = "application/hal+json",
            examples = {
              @ExampleObject(
                  name = "No matches with messages found",
                  value = FIND_MATCHES_WITH_MESSAGES_OK_NO_MATCHES_RESPONSE),
              @ExampleObject(
                  name = "Found matches with messages",
                  value = FIND_MATCHES_WITH_MESSAGES_OK_WITH_MATCHES_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "422",
      description = "UNPROCESSABLE ENTITY",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(
                  name = "Profile not exists",
                  value =
                      FIND_MATCHES_WITH_MESSAGES_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            })
      })
  @GetMapping("/conversations")
  public ResponseEntity<Object> findMatchesWithMessages() {
    return matchService.findAllMatchesWithMessages();
  }

  @Operation(
      summary = "Delete match by the id",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "204",
      description = "NO CONTENT",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(name = "Match deleted", value = DELETE_MATCH_NO_CONTENT_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "422",
      description = "UNPROCESSABLE ENTITY",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(
                  name = "Match not exists",
                  value = DELETE_MATCH_UNPROCESSABLE_ENTITY_MATCH_NOT_EXISTS_RESPONSE),
              @ExampleObject(
                  name = "Profile not exists",
                  value = DELETE_MATCH_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            }),
        @Content(
            mediaType = "application/json",
            examples = {
              @ExampleObject(
                  name = "Not valid delete request",
                  value = DELETE_MATCH_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE)
            })
      })
  @DeleteMapping("/matches/{id}")
  public ResponseEntity<Void> deleteMatch(
      @PathVariable @Positive(message = "The match id must be a positive number") Long id) {
    return matchService.deleteMatchById(id);
  }
}
