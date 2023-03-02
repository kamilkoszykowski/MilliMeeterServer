package millimeeter.server.controller;

import millimeeter.server.model.Profile;
import millimeeter.server.service.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.matchesRegex;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SwipeControllerTests {

  private final TestUtils testUtils;
  private final WebApplicationContext applicationContext;
  private MockMvc mockMvc;

  @Autowired
  public SwipeControllerTests(
      TestUtils testUtils,
      WebApplicationContext applicationContext) {
    this.testUtils = testUtils;
    this.applicationContext = applicationContext;
  }

  static final String USERNAME = TestUtils.USERNAME;
  private Profile profile;
  private Profile anotherProfile;

  @BeforeEach
  void init() {
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(applicationContext)
            .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
            .build();
    testUtils.createUserIfNotExists(TestUtils.USERNAME, testUtils.getProfile());
    testUtils.createUserIfNotExists(TestUtils.ANOTHER_USERNAME, testUtils.getAnotherProfile());
    //testUtils.deleteProfileIfExistsById(testUtils.getProfile());
    testUtils.createProfileIfNotExists(testUtils.getProfile());
    profile = testUtils.getProfile();
    anotherProfile = testUtils.getAnotherProfile();
  }

  @ParameterizedTest
  @WithMockUser(username = USERNAME)
  @ValueSource(strings = {"LEFT", "RIGHT"})
  void verifySwipeReturnCreatedSwipe(String direction) throws Exception {
    testUtils.deleteProfileIfExistsById(anotherProfile);
    testUtils.createProfileIfNotExists(anotherProfile);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherProfile.getId(), direction))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(greaterThan(0)))
        .andExpect(jsonPath("$.senderId").value(profile.getId()))
        .andExpect(jsonPath("$.receiverId").value(anotherProfile.getId()))
        .andExpect(jsonPath("$.direction").value(direction))
        .andExpect(
            jsonPath("$._links.['swipes left amount'].href")
                .value("http://localhost/api/v1/profiles/swipesLeft"));
  }

  @ParameterizedTest
  @WithMockUser(username = USERNAME)
  @ValueSource(strings = {"LEFT", "RIGHT"})
  void verifySwipingWithoutEnoughSwipesReturnUnprocessableEntity(String direction) throws Exception {
    testUtils.deleteProfileIfExistsById(profile);
    Profile unmodifiedProfile = testUtils.getProfile();
    Profile modifiedProfile = new Profile(unmodifiedProfile);
    modifiedProfile.setSwipesLeft(0);
    testUtils.createProfileIfNotExists(modifiedProfile);
    mockMvc
            .perform(post("/api/v1/swipes/{id}/{direction}", anotherProfile.getId(), direction))
            .andDo(print())
            .andDo(result -> {
              testUtils.deleteProfileIfExistsById(profile);
              testUtils.createProfileIfNotExists(unmodifiedProfile);
            })
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors").value("No swipes left"));
  }

  @ParameterizedTest
  @WithMockUser(username = USERNAME)
  @ValueSource(strings = {"LEFT", "RIGHT"})
  void verifySwipingYourselfReturnUnprocessableEntity(String direction) throws Exception {
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", profile.getId(), direction))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors").value("You swiped yourself"));
  }

  @ParameterizedTest
  @WithMockUser(username = USERNAME)
  @ValueSource(strings = {"LEFT", "RIGHT"})
  void verifySwipingAlreadySwipedProfileReturnConflict(String direction) throws Exception {
    testUtils.createProfileIfNotExists(anotherProfile);
    testUtils.addSwipe(profile.getId(), anotherProfile.getId(), direction);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherProfile.getId(), direction))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors").value("You already swiped that profile"));
  }

  @ParameterizedTest
  @WithMockUser(username = USERNAME)
  @ValueSource(strings = {"LEFT", "RIGHT"})
  void swipeLeftAndExpectNotFoundSwipedProfileNotExistsYet(String direction) throws Exception {
    testUtils.deleteProfileIfExistsById(anotherProfile);
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherProfile.getId(), direction))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors").value("Swiped profile not exists"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifySwipeRightReturnCreatedMatch() throws Exception {
    testUtils.deleteProfileIfExistsById(anotherProfile);
    testUtils.createProfileIfNotExists(anotherProfile);
    testUtils.addSwipe(anotherProfile.getId(), profile.getId(), "RIGHT");
    mockMvc
        .perform(post("/api/v1/swipes/{id}/{direction}", anotherProfile.getId(), "RIGHT"))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(greaterThan(0)))
        .andExpect(jsonPath("$.profileId1").value(profile.getId()))
        .andExpect(jsonPath("$.profileId2").value(anotherProfile.getId()))
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
}
