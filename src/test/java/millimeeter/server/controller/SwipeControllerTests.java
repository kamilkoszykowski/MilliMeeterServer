package millimeeter.server.controller;

import static millimeeter.server.controller.response.SwipeControllerResponses.*;
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

  @Autowired UserRepository userRepository;
  @Autowired ProfileRepository profileRepository;
  @Autowired SwipeRepository swipeRepository;
  @Autowired private WebApplicationContext applicationContext;
  private MockMvc mockMvc;

  static final String USERNAME = "mockUser";
  long profileId = -1;
  Profile profile =
      new Profile(
          null,
          "ProfileOne",
          LocalDate.parse("2000-01-01"),
          Gender.MAN,
          List.of("photo1.jpg", "photo2.jpg"),
          "description1",
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
  static final String ANOTHER_USERNAME = "anotherUser";
  long anotherId = -1;
  Profile anotherProfile =
      new Profile(
          null,
          "ProfileTwo",
          LocalDate.parse("2000-01-01"),
          Gender.WOMAN,
          List.of("anotherPhoto1.jpg", "anotherPhoto2.jpg"),
          "another description",
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
  void swipeLeftAndExpectUnprocessableEntityNotValid() throws Exception {
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", -1, "L"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("The swipe value must be LEFT or RIGHT"))
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("Profile id must be a positive number"))
        .andExpect(jsonPath("$.errors", hasSize(2)));
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
        .andExpect(jsonPath("$").value(SWIPE_UNPROCESSABLE_ENTITY_NO_SWIPES_LEFT_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeLeftAndExpectUnprocessableEntityCannotSwipeYourself() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", profileId, "LEFT"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$").value(SWIPE_UNPROCESSABLE_ENTITY_CANNOT_SWIPE_YOURSELF_RESPONSE));
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
        .andExpect(jsonPath("$").value(SWIPE_CONFLICT_ALREADY_SWIPED_RESPONSE));
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
        .andExpect(jsonPath("$").value(SWIPE_NOT_FOUND_SWIPED_PROFILE_NOT_EXISTS_YET_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeLeftAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherId, "LEFT"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$").value(SWIPE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
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
  void swipeRightAndExpectUnprocessableEntityNotValid() throws Exception {
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", -1, "R"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("Profile id must be a positive number"))
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("The swipe value must be LEFT or RIGHT"))
        .andExpect(jsonPath("$.errors", hasSize(2)));
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
        .andExpect(jsonPath("$").value(SWIPE_UNPROCESSABLE_ENTITY_NO_SWIPES_LEFT_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeRightAndExpectUnprocessableEntityCannotSwipeYourself() throws Exception {
    deleteProfileIfExistsById(profileId);
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", profileId, "RIGHT"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$").value(SWIPE_UNPROCESSABLE_ENTITY_CANNOT_SWIPE_YOURSELF_RESPONSE));
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
        .andExpect(jsonPath("$").value(SWIPE_CONFLICT_ALREADY_SWIPED_RESPONSE));
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
        .andExpect(jsonPath("$").value(SWIPE_NOT_FOUND_SWIPED_PROFILE_NOT_EXISTS_YET_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void swipeRightAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherId, "RIGHT"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$").value(SWIPE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }
}
