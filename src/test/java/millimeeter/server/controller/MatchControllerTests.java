package millimeeter.server.controller;

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
import java.util.List;
import millimeeter.server.dto.SentMessageDto;
import millimeeter.server.model.*;
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

  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;
  private final SwipeRepository swipeRepository;
  private final MatchRepository matchRepository;
  private final MessageRepository messageRepository;
  private final WebApplicationContext applicationContext;
  private MockMvc mockMvc;

  @Autowired
  public MatchControllerTests(
      UserRepository userRepository,
      ProfileRepository profileRepository,
      SwipeRepository swipeRepository,
      MatchRepository matchRepository,
      MessageRepository messageRepository,
      WebApplicationContext applicationContext) {
    this.userRepository = userRepository;
    this.profileRepository = profileRepository;
    this.swipeRepository = swipeRepository;
    this.matchRepository = matchRepository;
    this.messageRepository = messageRepository;
    this.applicationContext = applicationContext;
  }

  static final String USERNAME = MessageControllerTests.USERNAME;
  long profileId = -1;
  Profile profile = MessageControllerTests.profile;
  static final String ANOTHER_USERNAME = MessageControllerTests.ANOTHER_USERNAME;
  long anotherId = -1;
  Profile anotherProfile = MessageControllerTests.anotherProfile;

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
        .andExpect(jsonPath("$.errors").value("Match not exists"));
  }
}
