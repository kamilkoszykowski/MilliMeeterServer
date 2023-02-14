package millimeeter.server.controller;

import static millimeeter.server.controller.response.SwipeControllerResponses.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import millimeeter.server.service.SwipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Validated
@Tag(name = "Swipe")
@RequestMapping("/api/v1/swipes")
public class SwipeController {

  @Autowired private SwipeService swipeService;

  @Operation(
      summary = "Swipe profile with given id",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "201",
      description = "CREATED",
      content = {
        @Content(
            mediaType = "application/hal+json",
            examples = {
              @ExampleObject(name = "Swiped left", value = SWIPE_LEFT_CREATED_SWIPE_RESPONSE),
              @ExampleObject(
                  name = "Swiped right and matched",
                  value = SWIPE_RIGHT_CREATED_MATCH_RESPONSE),
              @ExampleObject(name = "Swiped right", value = SWIPE_RIGHT_CREATED_SWIPE_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "409",
      description = "CONFLICT",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(
                  name = "Swiped left already swiped",
                  value = SWIPE_CONFLICT_ALREADY_SWIPED_RESPONSE),
              @ExampleObject(
                  name = "Swiped right already swiped",
                  value = SWIPE_CONFLICT_ALREADY_SWIPED_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "404",
      description = "NOT EXISTS",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(
                  name = "Swiped left not existing profile",
                  value = SWIPE_NOT_FOUND_SWIPED_PROFILE_NOT_EXISTS_YET_RESPONSE),
              @ExampleObject(
                  name = "Swiped right not existing profile",
                  value = SWIPE_NOT_FOUND_SWIPED_PROFILE_NOT_EXISTS_YET_RESPONSE)
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
                  name = "Swiped left yourself",
                  value = SWIPE_UNPROCESSABLE_ENTITY_CANNOT_SWIPE_YOURSELF_RESPONSE),
              @ExampleObject(
                  name = "Swiped left with no swipes left",
                  value = SWIPE_UNPROCESSABLE_ENTITY_NO_SWIPES_LEFT_RESPONSE),
              @ExampleObject(
                  name = "Profile not exists",
                  value = SWIPE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE),
              @ExampleObject(
                  name = "Swiped right yourself",
                  value = SWIPE_UNPROCESSABLE_ENTITY_CANNOT_SWIPE_YOURSELF_RESPONSE),
              @ExampleObject(
                  name = "Swiped right with no swipes left",
                  value = SWIPE_UNPROCESSABLE_ENTITY_NO_SWIPES_LEFT_RESPONSE),
              @ExampleObject(
                  name = "Profile not exists",
                  value = SWIPE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            }),
        @Content(
            mediaType = "application/json",
            examples = {
              @ExampleObject(
                  name = "Not valid swipe left request",
                  value = SWIPE_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE),
              @ExampleObject(
                  name = "Not valid swipe right request",
                  value = SWIPE_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE)
            })
      })
  @PostMapping("/{id}/{direction}")
  public ResponseEntity<Object> swipe(
      @PathVariable @Positive(message = "Profile id must be a positive number") Long id,
      @PathVariable
          @Pattern(regexp = "LEFT|RIGHT", message = "The swipe value must be LEFT or RIGHT")
          String direction) {
    return swipeService.swipe(id, direction);
  }
}
