package millimeeter.server.controller;

import static millimeeter.server.controller.response.MatchControllerResponses.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import millimeeter.server.dto.SentMessageDto;
import millimeeter.server.enums.Gender;
import millimeeter.server.enums.LookingFor;
import millimeeter.server.model.Match;
import millimeeter.server.model.Message;
import millimeeter.server.model.Profile;
import millimeeter.server.model.Swipe;
import millimeeter.server.repository.*;
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
class MatchControllerTests {

  @Autowired UserRepository userRepository;
  @Autowired ProfileRepository profileRepository;
  @Autowired SwipeRepository swipeRepository;
  @Autowired MatchRepository matchRepository;
  @Autowired MessageRepository messageRepository;
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

  void addMatch(Long profileId1, Long profileId2) {
    matchRepository.save(new Match(profileId1, profileId2));
  }

  void addMessage(Long matchId, Long from, String content) {
    SentMessageDto sentMessageDto = new SentMessageDto(matchId, content, null);
    messageRepository.save(new Message(from, sentMessageDto));
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
  void getMatchesAndExpectOkNoMatches() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(get("/api/v1/matches"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void getMatchesAndExpectOkOneMatch() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    addMatch(profileId, anotherId);
    mockMvc
        .perform(get("/api/v1/matches"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.matchDtoList[0].id", greaterThan(0)))
        .andExpect(jsonPath("$._embedded.matchDtoList[0].profileId").value(anotherId))
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
  void getMatchesAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(get("/api/v1/matches"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$").value(FIND_MATCHES_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void getMatchesWithMessagesAndExpectOkNoMatches() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    addMatch(profileId, anotherId);
    mockMvc
        .perform(get("/api/v1/conversations"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void getMatchesWithMessagesAndExpectOkOneMatch() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    addMessage(match.getId(), profileId, "content");
    mockMvc
        .perform(get("/api/v1/conversations"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.matchWithMessagesDtoList[0].id", greaterThan(0)))
        .andExpect(jsonPath("$._embedded.matchWithMessagesDtoList[0].profileId").value(anotherId))
        .andExpect(
            jsonPath("$._embedded.matchWithMessagesDtoList[0].firstName")
                .value(anotherProfile.getFirstName()))
        .andExpect(jsonPath("$._embedded.matchWithMessagesDtoList[0].photos", hasSize(2)))
        .andExpect(jsonPath("$._embedded.matchWithMessagesDtoList[0].senderId").value(profileId))
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
  void getMatchesWithMessagesAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(get("/api/v1/conversations"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$")
                .value(
                    FIND_MATCHES_WITH_MESSAGES_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void deleteMatchAndExpectNoContent() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    mockMvc
        .perform(delete("/api/v1/matches/{id}", match.getId()))
        .andDo(print())
        .andExpect(status().isNoContent())
        .andExpect(jsonPath("$").doesNotExist());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void deleteMatchAndExpectUnprocessableEntityNotValid() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(delete("/api/v1/matches/{id}", -1))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("The match id must be a positive number"))
        .andExpect(jsonPath("$.errors", hasSize(1)));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void deleteMatchAndExpectUnprocessableEntityMatchNotExists() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    deleteProfileIfExistsById(anotherId);
    mockMvc
        .perform(delete("/api/v1/matches/{id}", match.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$").value(DELETE_MATCH_UNPROCESSABLE_ENTITY_MATCH_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void deleteMatchAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(delete("/api/v1/matches/{id}", 1))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$").value(DELETE_MATCH_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }
}
