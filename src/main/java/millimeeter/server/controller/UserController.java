package millimeeter.server.controller;

import static millimeeter.server.controller.response.UserControllerResponses.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import millimeeter.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Tag(name = "User")
@RequestMapping("/api/v1/users")
public class UserController {

  @Autowired private UserService userService;

  @Operation(
      summary = "Create user",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "201",
      description = "CREATED",
      content = {
        @Content(
            mediaType = "application/hal+json",
            examples = {
              @ExampleObject(name = "User created", value = CREATE_USER_CREATED_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "409",
      description = "CONFLICT",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(name = "User already exists", value = CREATE_USER_CONFLICT_RESPONSE)
            })
      })
  @PostMapping
  public ResponseEntity<Object> create() {
    return userService.create();
  }
}
