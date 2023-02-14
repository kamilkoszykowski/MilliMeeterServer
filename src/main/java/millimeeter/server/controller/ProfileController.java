package millimeeter.server.controller;

import static millimeeter.server.controller.response.ProfileControllerResponses.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import millimeeter.server.dto.LocationDto;
import millimeeter.server.dto.RegistrationDto;
import millimeeter.server.dto.UpdatedProfileDto;
import millimeeter.server.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@Validated
@Tag(name = "Profile")
@RequestMapping("/api/v1/profiles")
public class ProfileController {

  @Autowired private ProfileService profileService;

  @Operation(
      summary = "Find profiles to swipe",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = "application/hal+json",
            examples = {
              @ExampleObject(
                  name = "Found profiles to swipe",
                  value = FIND_PROFILES_TO_SWIPE_OK_WITH_PROFILES),
              @ExampleObject(
                  name = "No profiles to swipe found",
                  value = FIND_PROFILES_TO_SWIPE_OK_NO_PROFILES_RESPONSE)
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
                  value = FIND_PROFILES_TO_SWIPE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            })
      })
  @GetMapping
  public ResponseEntity<Object> findProfilesToSwipe() {
    return profileService.findProfilesToSwipe();
  }

  @Operation(
      summary = "Get my profile",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = "application/hal+json",
            examples = {
              @ExampleObject(name = "Got my profile", value = GET_MY_PROFILE_OK_RESPONSE)
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
                  name = "Profile not exist",
                  value = GET_MY_PROFILE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            })
      })
  @GetMapping("/me")
  public ResponseEntity<Object> getMyProfile() {
    return profileService.getMyProfile();
  }

  @Operation(
      summary = "Create the profile",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "201",
      description = "CREATED",
      content = {
        @Content(
            mediaType = "application/hal+json",
            examples = {@ExampleObject(name = "Profile created", value = CREATE_CREATED_RESPONSE)})
      })
  @ApiResponse(
      responseCode = "409",
      description = "CONFLICT",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(name = "Profile already exists", value = CREATE_CONFLICT_RESPONSE)
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
                  name = "Profile not created, too many photos uploaded",
                  value = CREATE_UNPROCESSABLE_ENTITY_TOO_MANY_PHOTOS_RESPONSE)
            })
      })
  @PostMapping()
  public ResponseEntity<Object> create(
      @Valid @RequestPart("body") RegistrationDto registrationDto,
      @RequestPart("photos") MultipartFile[] photos) {
    return profileService.create(registrationDto, photos);
  }

  @Operation(
      summary = "Update the profile",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = "application/hal+json",
            examples = {
              @ExampleObject(name = "Profile updated", value = UPDATE_PROFILE_OK_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "400",
      description = "BAD REQUEST",
      content = {
        @Content(
            mediaType = "application/json",
            examples = {
              @ExampleObject(name = "Bad request", value = UPDATE_PROFILE_BAD_REQUEST_RESPONSE)
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
                  value = UPDATE_PROFILE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            }),
        @Content(
            mediaType = "application/json",
            examples = {
              @ExampleObject(
                  name = "Not valid update request",
                  value = UPDATE_PROFILE_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE)
            })
      })
  @PutMapping
  public ResponseEntity<Object> update(@Valid @RequestBody UpdatedProfileDto updatedProfileDto) {
    return profileService.update(updatedProfileDto);
  }

  @Operation(
      summary = "Update the location",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = "application/hal+json",
            examples = {
              @ExampleObject(
                  name = "Location updated",
                  value = UPDATE_LOCATION_OK_AND_NEW_VALUES_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "400",
      description = "BAD REQUEST",
      content = {
        @Content(
            mediaType = "application/json",
            examples = {
              @ExampleObject(name = "Bad request", value = UPDATE_LOCATION_BAD_REQUEST_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "422",
      description = "UNPROCESSABLE ENTITY",
      content = {
        @Content(
            mediaType = "application/json",
            examples = {
              @ExampleObject(
                  name = "Not valid update request",
                  value = UPDATE_LOCATION_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE),
              @ExampleObject(
                  name = "Profile not exists",
                  value = UPDATE_LOCATION_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            })
      })
  @PutMapping("/location")
  public ResponseEntity<Object> updateLocation(@Valid @RequestBody LocationDto location) {
    return profileService.updateLocation(location);
  }

  @Operation(
      summary = "Upload the photo",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = "application/hal+json",
            examples = {
              @ExampleObject(
                  name = "Uploaded photo as last",
                  value = UPLOAD_PHOTO_OK_WITH_PHOTO_ADDED_AS_LAST_RESPONSE),
              @ExampleObject(
                  name = "Uploaded photo at certain index",
                  value = UPLOAD_PHOTO_OK_WITH_PHOTO_ADDED_AT_CERTAIN_INDEX_RESPONSE)
            })
      })
  @ApiResponse(
      responseCode = "400",
      description = "BAD REQUEST",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(name = "Bad request", value = UPLOAD_PHOTO_BAD_REQUEST_RESPONSE)
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
                  name = "Photo not uploaded, limit already reached",
                  value = UPLOAD_PHOTO_UNPROCESSABLE_ENTITY_PHOTO_LIMIT_REACHED_RESPONSE),
              @ExampleObject(
                  name = "Profile not exists",
                  value = UPLOAD_PHOTO_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            }),
        @Content(
            mediaType = "application/json",
            examples = {
              @ExampleObject(
                  name = "Not valid photo upload request",
                  value = UPLOAD_PHOTO_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE)
            })
      })
  @PutMapping("/photos")
  public ResponseEntity<Object> uploadPhoto(
      @RequestParam("index")
          @Min(value = -1, message = "Photo number must be between -1 and 4")
          @Max(value = 4, message = "Photo number must be between -1 and 4")
          int index,
      @RequestPart("photos") MultipartFile photo) {
    return profileService.uploadPhoto(index, photo);
  }

  @Operation(
      summary = "Delete the photo by index",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = "application/json",
            examples = {@ExampleObject(name = "Photo deleted", value = DELETE_PHOTO_OK_RESPONSE)})
      })
  @ApiResponse(
      responseCode = "400",
      description = "BAD REQUEST",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(name = "Bad request", value = DELETE_PHOTO_BAD_REQUEST_RESPONSE)
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
                  value = DELETE_PHOTO_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            }),
        @Content(
            mediaType = "application/json",
            examples = {
              @ExampleObject(
                  name = "Not valid delete photo request",
                  value = DELETE_PHOTO_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE)
            })
      })
  @PutMapping("/photos/{index}")
  public ResponseEntity<Object> deletePhoto(
      @PathVariable
          @Min(value = -1, message = "Photo number must be between -1 and 4")
          @Max(value = 4, message = "Photo number must be between -1 and 4")
          int index) {
    return profileService.deletePhoto(index);
  }

  @Operation(
      summary = "Delete my profile",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "204",
      description = "NO CONTENT",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(name = "Profile deleted", value = DELETE_PROFILE_NO_CONTENT_RESPONSE)
            })
      })
  @DeleteMapping
  public ResponseEntity<Void> deleteProfile() {
    return profileService.deleteMyProfile();
  }

  @Operation(
      summary = "Get swipes left count",
      security = {@SecurityRequirement(name = "bearer-key")})
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = {
        @Content(
            mediaType = "text/plain",
            examples = {
              @ExampleObject(name = "Got swipes left number", value = GET_SWIPES_LEFT_OK_RESPONSE)
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
                  value = GET_SWIPES_LEFT_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE)
            })
      })
  @GetMapping("/swipesLeft")
  public ResponseEntity<Object> getSwipesLeftCount() {
    return profileService.getSwipesLeftCount();
  }

  @Operation(
      summary = "GET test endpoint",
      security = {@SecurityRequirement(name = "bearer-key")})
  @GetMapping("/test/getUserId")
  public String test() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }
}
