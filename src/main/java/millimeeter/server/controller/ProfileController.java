package millimeeter.server.controller;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import millimeeter.server.dto.*;
import millimeeter.server.model.Profile;
import millimeeter.server.service.ProfileService;
import millimeeter.server.service.assembler.MyProfileDtoModelAssembler;
import millimeeter.server.service.assembler.ProfileModelAssembler;
import millimeeter.server.service.assembler.ProfileToSwipeDtoModelAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@Validated
@RequestMapping("/api/v1/profiles")
public class ProfileController {

  private final ProfileService profileService;
  private final MyProfileDtoModelAssembler myProfileDtoModelAssembler;
  private final ProfileModelAssembler profileModelAssembler;
  private final ProfileToSwipeDtoModelAssembler profileToSwipeDtoModelAssembler;

  @Autowired
  public ProfileController(
      ProfileService profileService,
      MyProfileDtoModelAssembler myProfileDtoModelAssembler,
      ProfileModelAssembler profileModelAssembler,
      ProfileToSwipeDtoModelAssembler profileToSwipeDtoModelAssembler) {
    this.profileService = profileService;
    this.myProfileDtoModelAssembler = myProfileDtoModelAssembler;
    this.profileModelAssembler = profileModelAssembler;
    this.profileToSwipeDtoModelAssembler = profileToSwipeDtoModelAssembler;
  }

  @GetMapping
  public ResponseEntity<CollectionModel<EntityModel<ProfileToSwipeDto>>> findProfilesToSwipe() {
    List<EntityModel<ProfileToSwipeDto>> profilesToSwipe =
        profileService.findProfilesToSwipe().stream()
            .map(profileToSwipeDtoModelAssembler::toModel)
            .collect(Collectors.toList());
    return new ResponseEntity<>(CollectionModel.of(profilesToSwipe), HttpStatus.OK);
  }

  @GetMapping("/me")
  public ResponseEntity<EntityModel<MyProfileDto>> getMyProfile() {
    return new ResponseEntity<>(
        myProfileDtoModelAssembler.toModel(profileService.getMyProfile()), HttpStatus.OK);
  }

  @PostMapping()
  public ResponseEntity<EntityModel<Profile>> create(
      @Valid @RequestPart("body") RegistrationDto registrationDto,
      @RequestPart("photos") MultipartFile[] photos) {
    return new ResponseEntity<>(
        profileModelAssembler.toModel(profileService.create(registrationDto, photos)),
        HttpStatus.CREATED);
  }

  @PutMapping
  public ResponseEntity<EntityModel<Profile>> update(
      @Valid @RequestBody UpdatedProfileDto updatedProfileDto) {
    return new ResponseEntity<>(
        profileModelAssembler.toModel(profileService.update(updatedProfileDto)), HttpStatus.OK);
  }

  @PutMapping("/location")
  public ResponseEntity<EntityModel<Profile>> updateLocation(
      @Valid @RequestBody LocationDto location) {
    return new ResponseEntity<>(
        profileModelAssembler.toModel(profileService.updateLocation(location)), HttpStatus.OK);
  }

  @PutMapping("/photos")
  public ResponseEntity<EntityModel<Profile>> uploadPhoto(
      @RequestParam("index")
          @Min(value = -1, message = "Photo number must be between -1 and 4")
          @Max(value = 4, message = "Photo number must be between -1 and 4")
          int index,
      @RequestPart("photos") MultipartFile photo) {
    return new ResponseEntity<>(
        profileModelAssembler.toModel(profileService.uploadPhoto(index, photo)), HttpStatus.OK);
  }

  @PutMapping("/photos/{index}")
  public ResponseEntity<Object> deletePhoto(
      @PathVariable
          @Min(value = 0, message = "Photo number must be between 0 and 4")
          @Max(value = 4, message = "Photo number must be between 0 and 4")
          int index) {
    return new ResponseEntity<>(profileService.deletePhoto(index), HttpStatus.OK);
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteProfile() {
    profileService.deleteMyProfile();
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/swipesLeft")
  public ResponseEntity<Integer> getSwipesLeftCount() {
    return new ResponseEntity<>(profileService.getSwipesLeftCount(), HttpStatus.OK);
  }
}
