package millimeeter.server.controller;

import millimeeter.server.model.Match;
import millimeeter.server.model.Profile;
import millimeeter.server.service.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MatchControllerTests {

  private final TestUtils testUtils;
  private final WebApplicationContext applicationContext;
  private MockMvc mockMvc;

  @Autowired
  public MatchControllerTests(
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
    testUtils.createProfileIfNotExists(testUtils.getProfile());
    testUtils.deleteProfileIfExistsById(testUtils.getAnotherProfile());
    testUtils.createProfileIfNotExists(testUtils.getAnotherProfile());
    profile = testUtils.getProfile();
    anotherProfile = testUtils.getAnotherProfile();
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyValidRequestReturnOkNoMatches() throws Exception {
    testUtils.deleteProfileIfExistsById(testUtils.getAnotherProfile());
    mockMvc
        .perform(get("/api/v1/matches"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyValidRequestReturnOkOneMatch() throws Exception {
    testUtils.addMatch(profile.getId(), anotherProfile.getId());
    mockMvc
        .perform(get("/api/v1/matches"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.matchDtoList[0].id", greaterThan(0)))
        .andExpect(jsonPath("$._embedded.matchDtoList[0].profileId").value(anotherProfile.getId()))
        .andExpect(
            jsonPath("$._embedded.matchDtoList[0].firstName").value(anotherProfile.getFirstName()))
        .andExpect(jsonPath("$._embedded.matchDtoList[0].photos", hasSize(2)))
        .andExpect(
            jsonPath(
                "$._embedded.matchDtoList[0]._links.conversation.href",
                matchesRegex("http://localhost/api/v1/conversations/\\d+")))
        .andExpect(
            jsonPath(
                "$._embedded.matchDtoList[0]._links.delete.href",
                matchesRegex("http://localhost/api/v1/matches/\\d+")));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyValidRequestReturnOkNoMatchesWithMessages() throws Exception {
    testUtils.addMatch(profile.getId(), anotherProfile.getId());
    mockMvc
        .perform(get("/api/v1/conversations"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyValidRequestReturnOkOneMatchWithMessages() throws Exception {
    Match match = testUtils.addMatch(profile.getId(), anotherProfile.getId());
    testUtils.addMessage(match.getId(), profile.getId(), "content");
    mockMvc
        .perform(get("/api/v1/conversations"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.matchWithMessagesDtoList[0].id", greaterThan(0)))
        .andExpect(jsonPath("$._embedded.matchWithMessagesDtoList[0].profileId").value(anotherProfile.getId()))
        .andExpect(
            jsonPath("$._embedded.matchWithMessagesDtoList[0].firstName")
                .value(anotherProfile.getFirstName()))
        .andExpect(jsonPath("$._embedded.matchWithMessagesDtoList[0].photos", hasSize(2)))
        .andExpect(jsonPath("$._embedded.matchWithMessagesDtoList[0].senderId").value(profile.getId()))
        .andExpect(
            jsonPath("$._embedded.matchWithMessagesDtoList[0].lastMessageContent").value("content"))
        .andExpect(
            jsonPath("$._embedded.matchWithMessagesDtoList[0].lastMessageStatus").value("SENT"))
        .andExpect(
            jsonPath(
                "$._embedded.matchWithMessagesDtoList[0]._links.conversation.href",
                matchesRegex("http://localhost/api/v1/conversations/\\d+")))
        .andExpect(
            jsonPath(
                "$._embedded.matchWithMessagesDtoList[0]._links.delete.href",
                matchesRegex("http://localhost/api/v1/matches/\\d+")));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyValidRequestDeleteMatchAndReturnNoContent() throws Exception {
    Match match = testUtils.addMatch(profile.getId(), anotherProfile.getId());
    mockMvc
        .perform(delete("/api/v1/matches/{id}", match.getId()))
        .andDo(print())
        .andExpect(status().isNoContent())
        .andExpect(jsonPath("$").doesNotExist());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyDeletingNotExistingMatchReturnNotFoundMatchNotExists() throws Exception {
    Match match = testUtils.addMatch(profile.getId(), anotherProfile.getId());
    testUtils.deleteProfileIfExistsById(anotherProfile);
    mockMvc
        .perform(delete("/api/v1/matches/{id}", match.getId()))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors").value("Match not exists"));
  }
}
