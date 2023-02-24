package millimeeter.server.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProfileService {

  private final ProfileRepository profileRepository;
  private final AuthUtils authUtils;
  static final String DIRECTORY = "photos/";

  @Autowired
  public ProfileService(ProfileRepository profileRepository, AuthUtils authUtils) {
    this.profileRepository = profileRepository;
    this.authUtils = authUtils;
  }

  public Profile create(RegistrationDto registrationDto, MultipartFile[] photos) {
    authUtils.checkIfUserExists();
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Profile already exists");
    } else {
      if (photos.length > 5) {
        throw new ResponseStatusException(
            HttpStatus.UNPROCESSABLE_ENTITY, "Limit of 5 photos exceeded");
      }
      List<String> photosData = new ArrayList<>();
      for (MultipartFile photo : photos) {
        photosData = savePhotoAndReturnUpdatedList(photosData, -1, photo);
      }
      Profile profile = new Profile(authUtils.getProfileId(), registrationDto, photosData);
      return profileRepository.save(profile);
    }
  }

  public Profile update(UpdatedProfileDto updatedProfileDto) {
    authUtils.checkIfProfileExists();
    Profile profile = authUtils.getProfile();
    profile.setDescription(updatedProfileDto.getDescription());
    profile.setMySong(updatedProfileDto.getMySong());
    profile.setLastLatitude(updatedProfileDto.getLastLatitude());
    profile.setLastLongitude(updatedProfileDto.getLastLongitude());
    profile.setLookingFor(LookingFor.valueOf(updatedProfileDto.getLookingFor()));
    profile.setSearchDistance(updatedProfileDto.getSearchDistance());
    profile.setAgeRangeMinimum(updatedProfileDto.getAgeRangeMinimum());
    profile.setAgeRangeMaximum(updatedProfileDto.getAgeRangeMaximum());
    return profileRepository.save(profile);
  }

  public Profile updateLocation(LocationDto location) {
    authUtils.checkIfProfileExists();
    Profile profile = authUtils.getProfile();
    profile.setLastLongitude(location.getLastLongitude());
    profile.setLastLatitude(location.getLastLatitude());
    return profileRepository.save(profile);
  }

  public Profile uploadPhoto(int index, MultipartFile photo) {
    authUtils.checkIfProfileExists();
    Profile profile = authUtils.getProfile();
    List<String> photos = profile.getPhotos();
    if (index == -1) {
      if (photos.size() < 5) {
        profile.setPhotos(savePhotoAndReturnUpdatedList(photos, index, photo));
        return profileRepository.save(profile);
      } else {
        throw new ResponseStatusException(
            HttpStatus.UNPROCESSABLE_ENTITY, "Limit of 5 photos reached");
      }
    } else {
      profile.setPhotos(savePhotoAndReturnUpdatedList(photos, index, photo));
      return profileRepository.save(profile);
    }
  }

  public int getSwipesLeftCount() {
    authUtils.checkIfProfileExists();
    Profile profile = authUtils.getProfile();
    if (profile.getWaitUntil() != null && LocalDateTime.now().isAfter(profile.getWaitUntil())) {
      profile.setSwipesLeft(50);
      profile.setWaitUntil(LocalDateTime.now().plusHours(12));
      profileRepository.save(profile);
      return profile.getSwipesLeft();
    }
    return profileRepository.getSwipesLeftById(authUtils.getProfileId());
  }

  public List<ProfileToSwipeDto> findProfilesToSwipe() {
    authUtils.checkIfProfileExists();
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
    return projectTupleToProfileToSwipeDtoList(profileTuples);
  }

  public MyProfileDto getMyProfile() {
    authUtils.checkIfProfileExists();
    return new MyProfileDto(authUtils.getProfile());
  }

  public void deleteMyProfile() {
    authUtils.checkIfUserExists();
    Long profileId = authUtils.getProfileId();
    if (authUtils.profileExistsById(profileId)) {
      profileRepository.deleteById(profileId);
    }
  }

  public Profile deletePhoto(int index) {
    authUtils.checkIfProfileExists();
    Profile profile = authUtils.getProfile();
    List<String> photos = profile.getPhotos();
    if (photos.size() < index + 1) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "Photo with the given number does not exist");
    }
    File toDelete = new File(DIRECTORY + photos.get(index));
    if (!toDelete.delete()) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "An error occurred during deleting the photo " + toDelete);
    }
    photos.remove(index);
    profile.setPhotos(photos);
    return profileRepository.save(profile);
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
                HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while deleting the photo");
          }
        }
      } else {
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating a new photo file");
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.out.println(e.getStackTrace());
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
