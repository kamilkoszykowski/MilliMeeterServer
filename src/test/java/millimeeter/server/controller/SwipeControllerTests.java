package millimeeter.server.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import millimeeter.server.enums.Gender;
import millimeeter.server.enums.LookingFor;
import millimeeter.server.model.Profile;
import millimeeter.server.model.Swipe;
import millimeeter.server.repository.ProfileRepository;
import millimeeter.server.repository.SwipeRepository;
import millimeeter.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
class SwipeControllerTests {

  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;
  private final SwipeRepository swipeRepository;
  private final WebApplicationContext applicationContext;
  private MockMvc mockMvc;

  @Autowired
  public SwipeControllerTests(
      UserRepository userRepository,
      ProfileRepository profileRepository,
      SwipeRepository swipeRepository,
      WebApplicationContext applicationContext) {
    this.userRepository = userRepository;
    this.profileRepository = profileRepository;
    this.swipeRepository = swipeRepository;
    this.applicationContext = applicationContext;
  }

  static final String USERNAME = "mockUser";
  long profileId = -1;
  static Profile profile =
      new Profile(
          null,
          "TestUser",
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
  static final String ANOTHER_USERNAME = "Gloria";
  long anotherId = -1;
  static Profile anotherProfile =
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
  static final String ANOTHER_USERNAME_2 = "Victoria";
  long anotherId2 = -1;
  static Profile anotherProfile2 =
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

  void addSwipe(Long from, Long to, String direction) {
    swipeRepository.save(new Swipe(from, to, direction));
  }

  void createProfileIfNotExists(Long profileId, Profile profile) {
    try {
      for (String photo : profile.getPhotos()) {
        FileOutputStream output = new FileOutputStream("photos/" + photo);
        output.write("0x80".getBytes());
        output.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (profileRepository.findById(profileId).isEmpty()) {
      profile.setId(profileId);
      profileRepository.save(profile);
    }
  }

  void deleteProfileIfExistsById(Long profileId) {
    if (profileRepository.findById(profileId).isPresent()) {
      List<String> photos = profileRepository.findById(profileId).get().getPhotos();
      for (String photo : photos) {
        File toDelete = new File("photos/" + photo);
        toDelete.delete();
      }
      profileRepository.deleteById(profileId);
    }
  }

  @BeforeEach
  void init() {
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(applicationContext)
            .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
            .build();
    if (userRepository.findById(USERNAME).isEmpty()) {
      profileId = userRepository.createUser(USERNAME).getProfileId();
    } else {
      profileId = userRepository.findProfileIdById(USERNAME);
    }
    if (userRepository.findById(ANOTHER_USERNAME).isEmpty()) {
      anotherId = userRepository.createUser(ANOTHER_USERNAME).getProfileId();
    } else {
      anotherId = userRepository.findProfileIdById(ANOTHER_USERNAME);
    }
    deleteProfileIfExistsById(profileId);
    createProfileIfNotExists(profileId, profile);
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeLeftAndExpectCreatedSwipe() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherId, "LEFT"))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(greaterThan(0)))
        .andExpect(jsonPath("$.senderId").value(profileId))
        .andExpect(jsonPath("$.receiverId").value(anotherId))
        .andExpect(jsonPath("$.direction").value("LEFT"))
        .andExpect(
            jsonPath("$._links.['swipes left amount'].href")
                .value("http://localhost/api/v1/profiles/swipesLeft"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeLeftAndExpectUnprocessableEntityNoSwipesLeft() throws Exception {
    deleteProfileIfExistsById(profileId);
    Profile modifiedProfile = profile;
    modifiedProfile.setSwipesLeft(0);
    createProfileIfNotExists(profileId, modifiedProfile);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherId, "LEFT"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors").value("No swipes left"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeLeftAndExpectUnprocessableEntityCannotSwipeYourself() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", profileId, "LEFT"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors").value("You swiped yourself"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeLeftAndExpectConflictAlreadySwiped() throws Exception {
    deleteProfileIfExistsById(profileId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    addSwipe(profileId, anotherId, "LEFT");
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherId, "LEFT"))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors").value("You already swiped that profile"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeLeftAndExpectNotFoundSwipedProfileNotExistsYet() throws Exception {
    deleteProfileIfExistsById(profileId);
    createProfileIfNotExists(profileId, profile);
    deleteProfileIfExistsById(anotherId);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherId, "LEFT"))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors").value("Swiped profile not exists"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeRightAndExpectCreatedSwipe() throws Exception {
    deleteProfileIfExistsById(profileId);
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherId, "RIGHT"))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(greaterThan(0)))
        .andExpect(jsonPath("$.senderId").value(profileId))
        .andExpect(jsonPath("$.receiverId").value(anotherId))
        .andExpect(jsonPath("$.direction").value("RIGHT"))
        .andExpect(
            jsonPath("$._links.['swipes left amount'].href")
                .value("http://localhost/api/v1/profiles/swipesLeft"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeRightAndExpectCreatedMatch() throws Exception {
    deleteProfileIfExistsById(profileId);
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    addSwipe(anotherId, profileId, "RIGHT");
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherId, "RIGHT"))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(greaterThan(0)))
        .andExpect(jsonPath("$.profileId1").value(profileId))
        .andExpect(jsonPath("$.profileId2").value(anotherId))
        .andExpect(
            jsonPath(
                "$._links.conversation.href",
                matchesRegex("http://localhost/api/v1/conversations/\\d+")))
        .andExpect(
            jsonPath("$._links.['send message'].href").value("http://localhost/api/v1/messages"))
        .andExpect(
            jsonPath("$._links.delete.href", matchesRegex("http://localhost/api/v1/matches/\\d+")))
        .andExpect(
            jsonPath("$._links.['swipes left amount'].href")
                .value("http://localhost/api/v1/profiles/swipesLeft"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeRightAndExpectUnprocessableEntityNoSwipesLeft() throws Exception {
    deleteProfileIfExistsById(profileId);
    Profile modifiedProfile = profile;
    modifiedProfile.setSwipesLeft(0);
    createProfileIfNotExists(profileId, modifiedProfile);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherId, "RIGHT"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors").value("No swipes left"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeRightAndExpectUnprocessableEntityCannotSwipeYourself() throws Exception {
    deleteProfileIfExistsById(profileId);
    profile.setSwipesLeft(50);
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", profileId, "RIGHT"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors").value("You swiped yourself"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeRightAndExpectConflictAlreadySwiped() throws Exception {
    deleteProfileIfExistsById(profileId);
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    addSwipe(profileId, anotherId, "RIGHT");
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherId, "RIGHT"))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors").value("You already swiped that profile"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeRightAndExpectNotFoundSwipedProfileNotExistsYet() throws Exception {
    deleteProfileIfExistsById(profileId);
    createProfileIfNotExists(profileId, profile);
    deleteProfileIfExistsById(anotherId);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherId, "RIGHT"))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors").value("Swiped profile not exists"));
  }
}
