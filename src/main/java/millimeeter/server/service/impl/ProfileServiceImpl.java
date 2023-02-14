package millimeeter.server.service.impl;

import static millimeeter.server.controller.response.ProfileControllerResponses.*;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.Tuple;
import millimeeter.server.dto.*;
import millimeeter.server.enums.Gender;
import millimeeter.server.enums.LookingFor;
import millimeeter.server.model.Profile;
import millimeeter.server.repository.ProfileRepository;
import millimeeter.server.service.AuthUtils;
import millimeeter.server.service.ProfileService;
import millimeeter.server.service.assembler.MyProfileDtoModelAssembler;
import millimeeter.server.service.assembler.ProfileModelAssembler;
import millimeeter.server.service.assembler.ProfileToSwipeDtoModelAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProfileServiceImpl implements ProfileService {

  @Autowired private ProfileRepository profileRepository;
  @Autowired private AuthUtils authUtils;
  @Autowired private MyProfileDtoModelAssembler myProfileDtoModelAssembler;
  @Autowired private ProfileModelAssembler profileModelAssembler;
  @Autowired private ProfileToSwipeDtoModelAssembler profileToSwipeDtoModelAssembler;

  static final String DIRECTORY = "photos/";

  @Override
  public ResponseEntity<Object> create(RegistrationDto registrationDto, MultipartFile[] photos) {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      return new ResponseEntity<>(CREATE_CONFLICT_RESPONSE, HttpStatus.CONFLICT);
    } else {
      if (photos.length > 5) {
        return new ResponseEntity<>(
            CREATE_UNPROCESSABLE_ENTITY_TOO_MANY_PHOTOS_RESPONSE, HttpStatus.UNPROCESSABLE_ENTITY);
      } else {
        List<String> photosData = new ArrayList<>();
        for (MultipartFile photo : photos) {
          photosData = savePhotoAndReturnUpdatedList(photosData, -1, photo);
        }
        Profile profile = new Profile(authUtils.getProfileId(), registrationDto, photosData);
        return new ResponseEntity<>(
            profileModelAssembler.toModel(profileRepository.save(profile)), HttpStatus.CREATED);
      }
    }
  }

  @Override
  public ResponseEntity<Object> update(UpdatedProfileDto updatedProfileDto) {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      Profile profile = authUtils.getProfile();
      profile.setDescription(updatedProfileDto.getDescription());
      profile.setMySong(updatedProfileDto.getMySong());
      profile.setLastLatitude(updatedProfileDto.getLastLatitude());
      profile.setLastLongitude(updatedProfileDto.getLastLongitude());
      profile.setLookingFor(LookingFor.valueOf(updatedProfileDto.getLookingFor()));
      profile.setSearchDistance(updatedProfileDto.getSearchDistance());
      profile.setAgeRangeMinimum(updatedProfileDto.getAgeRangeMinimum());
      profile.setAgeRangeMaximum(updatedProfileDto.getAgeRangeMaximum());
      return new ResponseEntity<>(
          profileModelAssembler.toModel(profileRepository.save(profile)), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(
          UPDATE_PROFILE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE,
          HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @Override
  public ResponseEntity<Object> updateLocation(LocationDto location) {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      Profile profile = authUtils.getProfile();
      profile.setLastLongitude(location.getLastLongitude());
      profile.setLastLatitude(location.getLastLatitude());
      return new ResponseEntity<>(
          profileModelAssembler.toModel(profileRepository.save(profile)), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(
          "Cannot update the location due to non-existing profile",
          HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @Override
  public ResponseEntity<Object> uploadPhoto(int index, MultipartFile photo) {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      Profile profile = authUtils.getProfile();
      List<String> photos = profile.getPhotos();
      if (index == -1) {
        if (photos.size() < 5) {
          profile.setPhotos(savePhotoAndReturnUpdatedList(photos, index, photo));
          return new ResponseEntity<>(
              profileModelAssembler.toModel(profileRepository.save(profile)), HttpStatus.OK);
        } else {
          return new ResponseEntity<>(
              UPLOAD_PHOTO_UNPROCESSABLE_ENTITY_PHOTO_LIMIT_REACHED_RESPONSE,
              HttpStatus.UNPROCESSABLE_ENTITY);
        }
      } else {
        profile.setPhotos(savePhotoAndReturnUpdatedList(photos, index, photo));
        return new ResponseEntity<>(
            profileModelAssembler.toModel(profileRepository.save(profile)), HttpStatus.OK);
      }
    } else {
      return new ResponseEntity<>(
          UPLOAD_PHOTO_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE,
          HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @Override
  public ResponseEntity<Object> getSwipesLeftCount() {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      Profile profile = authUtils.getProfile();
      if (profile.getWaitUntil() != null && LocalDateTime.now().isAfter(profile.getWaitUntil())) {
        profile.setSwipesLeft(50);
        profile.setWaitUntil(LocalDateTime.now().plusHours(12));
        profileRepository.save(profile);
        return new ResponseEntity<>(profile.getSwipesLeft(), HttpStatus.OK);
      }
      return new ResponseEntity<>(
          profileRepository.getSwipesLeftById(authUtils.getProfileId()), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(
          GET_SWIPES_LEFT_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE,
          HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @Override
  public ResponseEntity<Object> findProfilesToSwipe() {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      Profile profile = authUtils.getProfile();
      List<Tuple> profileTuples;
      Gender gender = convertLookingForToGender(profile.getLookingFor());
      if (gender != null) {
        profileTuples =
            profileRepository.findProfilesByGenderToSwipe(
                gender.toString(),
                profile.getId(),
                LocalDate.now().minusYears(profile.getAgeRangeMaximum()),
                LocalDate.now().minusYears(profile.getAgeRangeMinimum()),
                profile.getLastLongitude(),
                profile.getLastLatitude(),
                profile.getSearchDistance());
      } else {
        profileTuples =
            profileRepository.findProfilesToSwipe(
                profile.getId(),
                LocalDate.now().minusYears(profile.getAgeRangeMaximum()),
                LocalDate.now().minusYears(profile.getAgeRangeMinimum()),
                profile.getLastLongitude(),
                profile.getLastLatitude(),
                profile.getSearchDistance());
      }
      List<EntityModel<ProfileToSwipeDto>> profilesToSwipe =
          projectTupleToProfileToSwipeDtoList(profileTuples).stream()
              .map(profileToSwipeDtoModelAssembler::toModel)
              .collect(Collectors.toList());

      return new ResponseEntity<>(CollectionModel.of(profilesToSwipe), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(
          FIND_PROFILES_TO_SWIPE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE,
          HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @Override
  public ResponseEntity<Object> getMyProfile() {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      MyProfileDto myProfile = new MyProfileDto(authUtils.getProfile());
      return new ResponseEntity<>(myProfileDtoModelAssembler.toModel(myProfile), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(
          GET_MY_PROFILE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE,
          HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @Override
  public ResponseEntity<Void> deleteMyProfile() {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      profileRepository.deleteById(authUtils.getProfileId());
    }
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  public ResponseEntity<Object> deletePhoto(int index) {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      Profile profile = authUtils.getProfile();
      List<String> photos = profile.getPhotos();
      if (photos.size() < index + 1) {
        return new ResponseEntity<>(
            "Photo with the given number does not exist", HttpStatus.UNPROCESSABLE_ENTITY);
      } else {
        File toDelete = new File(DIRECTORY + photos.get(index));
        if (toDelete.delete()) {
          photos.remove(index);
          profile.setPhotos(photos);
          return new ResponseEntity<>(profileRepository.save(profile), HttpStatus.OK);
        } else {
          throw new ResponseStatusException(
              HttpStatus.INTERNAL_SERVER_ERROR,
              "An error occurred during deleting the photo " + toDelete);
        }
      }
    } else {
      return new ResponseEntity<>(
          DELETE_PHOTO_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE,
          HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  List<ProfileToSwipeDto> projectTupleToProfileToSwipeDtoList(List<Tuple> tupleList) {
    return tupleList.stream()
        .map(
            t ->
                new ProfileToSwipeDto(
                    t.get("id", BigInteger.class).longValue(),
                    t.get("first_name", String.class),
                    t.get("age", BigDecimal.class).intValue(),
                    Gender.valueOf(t.get("gender", String.class)),
                    t.get("photos", String.class).split(";"),
                    t.get("description", String.class),
                    t.get("my_song", String.class),
                    t.get("distance", Double.class).intValue()))
        .collect(Collectors.toList());
  }

  List<String> savePhotoAndReturnUpdatedList(List<String> photos, int index, MultipartFile photo) {
    try {
      String name =
          Calendar.getInstance().getTimeInMillis() + UUID.randomUUID().toString() + ".jpg";
      File path = new File(DIRECTORY + name);
      if (path.createNewFile()) {
        FileOutputStream output = new FileOutputStream(path);
        output.write(photo.getBytes());
        output.close();
        if (index == -1) {
          photos.add(path.getName());
        } else {
          File toDelete = new File(DIRECTORY + photos.get(index));
          if (toDelete.delete()) {
            photos.set(index, path.getName());
          } else {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred during deleting the photo");
          }
        }
      } else {
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating a new photo file");
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.out.println(e.getStackTrace().toString());
    }
    return photos;
  }

  public Gender convertLookingForToGender(LookingFor lookingFor) {
    return switch (lookingFor) {
      case MEN -> Gender.MAN;
      case WOMEN -> Gender.WOMAN;
      default -> null;
    };
  }
}
