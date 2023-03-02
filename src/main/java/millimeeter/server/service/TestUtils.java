package millimeeter.server.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import millimeeter.server.dto.RegistrationDto;
import millimeeter.server.dto.SentMessageDto;
import millimeeter.server.enums.Gender;
import millimeeter.server.enums.LookingFor;
import millimeeter.server.model.Match;
import millimeeter.server.model.Message;
import millimeeter.server.model.Profile;
import millimeeter.server.model.Swipe;
import millimeeter.server.repository.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class TestUtils {

  private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SwipeRepository swipeRepository;
    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;

  public TestUtils(
      UserRepository userRepository,
      ProfileRepository profileRepository,
      SwipeRepository swipeRepository,
      MatchRepository matchRepository,
      MessageRepository messageRepository) {
    this.userRepository = userRepository;
    this.profileRepository = profileRepository;
    this.swipeRepository = swipeRepository;
    this.matchRepository = matchRepository;
    this.messageRepository = messageRepository;
  }

  public static final String USERNAME = "mockUser";
  public static final String ANOTHER_USERNAME = "Gloria";
  public static final String ANOTHER_USERNAME_2 = "Victoria";

  private long profileId = -1;
  private long anotherId = -1;
  private long anotherId2 = -1;

  private Profile profile =
      new Profile(
          null,
          "Testuser",
          LocalDate.parse("2000-01-01"),
          Gender.MAN,
          List.of("photo1.jpg", "photo2.jpg"),
          "Hi, I'm user who takes part in integration testing :)",
          "mySong",
          90.0,
          90.0,
          LocalDateTime.now(),
          50,
          null,
          LookingFor.WOMEN,
          100,
          18,
          40);

  private Profile anotherProfile =
      new Profile(
          null,
          "Gloria",
          LocalDate.parse("2000-01-01"),
          Gender.WOMAN,
          List.of("anotherPhoto1.jpg", "anotherPhoto2.jpg"),
          "Hi, I'm another profile who takes part in integration testing of that API",
          "another mySong",
          89.0,
          89.0,
          LocalDateTime.now(),
          50,
          null,
          LookingFor.MEN,
          100,
          18,
          40);

  private Profile anotherProfile2 =
      new Profile(
          null,
          "Victoria",
          LocalDate.parse("2004-01-01"),
          Gender.WOMAN,
          List.of("anotherPhoto2_1.jpg", "anotherPhoto2_2.jpg"),
          "I'm helping too :3",
          "another mySong 2",
          88.0,
          88.0,
          LocalDateTime.now(),
          50,
          null,
          LookingFor.MEN,
          100,
          20,
          50);

  private RegistrationDto registrationDto =
      new RegistrationDto(
          profile.getFirstName(),
          profile.getDateOfBirth(),
          profile.getGender().name(),
          profile.getDescription(),
          profile.getMySong(),
          profile.getLastLatitude(),
          profile.getLastLongitude(),
          profile.getLookingFor().name(),
          profile.getSearchDistance(),
          profile.getAgeRangeMinimum(),
          profile.getAgeRangeMaximum());

  private String bodyJson =
      "{\n"
          + "    \"firstName\": \""
          + registrationDto.getFirstName()
          + "\",\n"
          + "    \"dateOfBirth\": \""
          + registrationDto.getDateOfBirth()
          + "\",\n"
          + "    \"gender\": \""
          + registrationDto.getGender()
          + "\",\n"
          + "    \"description\": \""
          + registrationDto.getDescription()
          + "\",\n"
          + "    \"mySong\": \""
          + registrationDto.getMySong()
          + "\",\n"
          + "    \"lastLatitude\": "
          + registrationDto.getLastLatitude()
          + ",\n"
          + "    \"lastLongitude\": "
          + registrationDto.getLastLongitude()
          + ",\n"
          + "    \"lookingFor\": \""
          + registrationDto.getLookingFor()
          + "\",\n"
          + "    \"searchDistance\": "
          + registrationDto.getSearchDistance()
          + ",\n"
          + "    \"ageRangeMinimum\": "
          + registrationDto.getAgeRangeMinimum()
          + ",\n"
          + "    \"ageRangeMaximum\": "
          + registrationDto.getAgeRangeMaximum()
          + "\n"
          + "}";

  static final String TEST_PHOTO_PATH = "src/main/resources/static/testPhoto.jpg";
  static final String INVALID_TEST_PHOTO_PATH = "src/main/resources/static/invalidTestPhoto.jpg";

  private MockMultipartFile photo =
      createMockFile("photos", "magik.jpg", MediaType.IMAGE_JPEG_VALUE, TEST_PHOTO_PATH);
  private MockMultipartFile invalidPhoto =
          createMockFile("photos", "invalid.jpg", MediaType.IMAGE_JPEG_VALUE, INVALID_TEST_PHOTO_PATH);
  private MockMultipartFile body =
      createMockFile("body", "test.json", MediaType.APPLICATION_JSON_VALUE, bodyJson);

  public static MockMultipartFile createMockFile(
      String name, String originalFilename, String mediaType, String path) {
    try {
      return new MockMultipartFile(
          name,
          originalFilename,
          mediaType,
          mediaType.equals("application/json")
              ? path.getBytes()
              : new FileInputStream(path).readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void createUserIfNotExists(String username, Profile profile) {
    if (userRepository.findById(username).isEmpty()) {
      profile.setId(userRepository.createUser(username).getProfileId());
    } else {
      profile.setId(userRepository.findProfileIdById(username));
    }
  }

  public void deleteUserIfExists(String username) {
    if (userRepository.findById(username).isPresent()) {
      userRepository.deleteById(username);
    }
  }

  public void createProfileIfNotExists(Profile profile) {
    try {
      for (String photo : profile.getPhotos()) {
        FileOutputStream output = new FileOutputStream("photos/" + photo);
        output.write("0x80".getBytes());
        output.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (profileRepository.findById(profile.getId()).isEmpty()) {
      profileRepository.save(profile);
    }
  }

  public void deleteProfileIfExistsById(Profile profile) {
    long id = profile.getId();
    if (profileRepository.findById(id).isPresent()) {
      List<String> photos = profileRepository.findById(id).get().getPhotos();
      for (String photo : photos) {
        File toDelete = new File("photos/" + photo);
        toDelete.delete();
      }
      profileRepository.deleteById(id);
    }
  }

  public Swipe addSwipe(Long from, Long to, String direction) {
    return swipeRepository.save(new Swipe(from, to, direction));
  }

  public Match addMatch(Long profileId1, Long profileId2) {
    return matchRepository.save(new Match(profileId1, profileId2));
  }

  public Message addMessage(Long matchId, Long from, String content) {
    SentMessageDto sentMessageDto = new SentMessageDto(matchId, content, null);
    return messageRepository.save(new Message(from, sentMessageDto));
  }

  public Message addMessage(Long senderId, SentMessageDto sentMessageDto) {
    return messageRepository.save(new Message(senderId, sentMessageDto));
  }
}
