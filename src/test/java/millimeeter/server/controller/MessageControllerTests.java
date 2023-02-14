package millimeeter.server.controller;

import static millimeeter.server.controller.response.MessageControllerResponses.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import millimeeter.server.enums.MessageReaction;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
class MessageControllerTests {

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
  static final String ANOTHER_USERNAME_2 = "anotherUser";
  long anotherId2 = -1;
  Profile anotherProfile2 =
      new Profile(
          null,
          "ProfileThree",
          LocalDate.parse("2004-01-01"),
          Gender.WOMAN,
          List.of("anotherPhoto2_1.jpg", "anotherPhoto2_2.jpg"),
          "another description 2",
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
    if (userRepository.findById(ANOTHER_USERNAME_2).isEmpty()) {
      anotherId2 = userRepository.createUser(ANOTHER_USERNAME_2).getProfileId();
    } else {
      anotherId2 = userRepository.findProfileIdById(ANOTHER_USERNAME_2);
    }
  }

  @Test
  @WithMockUser(username = USERNAME)
  void sendAndExpectCreated() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    mockMvc
        .perform(
            post("/api/v1/messages")
                .content(
                    "{\n"
                        + "  \"matchId\": "
                        + match.getId()
                        + ",\n"
                        + "  \"content\": \"content\",\n"
                        + "  \"parentMessageId\": null\n"
                        + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(greaterThan(0)))
        .andExpect(jsonPath("$.senderId").value(profileId))
        .andExpect(jsonPath("$.matchId").value(match.getId()))
        .andExpect(jsonPath("$.content").value("content"))
        .andExpect(jsonPath("$.parentMessageId").isEmpty())
        .andExpect(jsonPath("$.senderReaction").isEmpty())
        .andExpect(jsonPath("$.receiverReaction").isEmpty())
        .andExpect(jsonPath("$.status").value("SENT"))
        .andExpect(jsonPath("$.seenAt").isEmpty())
        .andExpect(jsonPath("$._links.reply.href").value("http://localhost/api/v1/messages"))
        .andExpect(
            jsonPath(
                "$._links.['react like'].href",
                matchesRegex("http://localhost/api/v1/messages/\\d+/LIKE")))
        .andExpect(
            jsonPath(
                "$._links.['react super'].href",
                matchesRegex("http://localhost/api/v1/messages/\\d+/SUPER")))
        .andExpect(
            jsonPath(
                "$._links.['react haha'].href",
                matchesRegex("http://localhost/api/v1/messages/\\d+/HAHA")))
        .andExpect(
            jsonPath(
                "$._links.['react cry'].href",
                matchesRegex("http://localhost/api/v1/messages/\\d+/CRY")))
        .andExpect(
            jsonPath(
                "$._links.['react wrr'].href",
                matchesRegex("http://localhost/api/v1/messages/\\d+/WRR")))
        .andExpect(
            jsonPath(
                "$._links.['react care'].href",
                matchesRegex("http://localhost/api/v1/messages/\\d+/CARE")));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void sendAndExpectUnprocessableEntityNotValid() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    mockMvc
        .perform(
            post("/api/v1/messages")
                .content(
                    "{\n"
                        + "  \"matchId\": "
                        + match.getId()
                        + ",\n"
                        + "  \"content\": \"content\",\n"
                        + "  \"parentMessageId\": -1\n"
                        + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("The parent message id must be a positive number"))
        .andExpect(jsonPath("$.errors", hasSize(1)));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void sendAndExpectUnprocessableEntityProfileNotBelongsToMatch() throws Exception {
    deleteProfileIfExistsById(anotherId);
    deleteProfileIfExistsById(anotherId2);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(anotherId, anotherId2));
    mockMvc
        .perform(
            post("/api/v1/messages")
                .content(
                    "{\n"
                        + "  \"matchId\": "
                        + match.getId()
                        + ",\n"
                        + "  \"content\": \"content\",\n"
                        + "  \"parentMessageId\": null\n"
                        + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$").value(UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void sendAndExpectUnprocessableEntityMatchNotExists() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    deleteProfileIfExistsById(anotherId);
    mockMvc
        .perform(
            post("/api/v1/messages")
                .content(
                    "{\n"
                        + "  \"matchId\": "
                        + match.getId()
                        + ",\n"
                        + "  \"content\": \"content\",\n"
                        + "  \"parentMessageId\": null\n"
                        + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$").value(UNPROCESSABLE_ENTITY_MATCH_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void sendAndExpectUnprocessableEntityParentMessageNotExists() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = messageRepository.save(new Message(profileId, sentMessageDto));
    messageRepository.deleteById(message.getId());
    mockMvc
        .perform(
            post("/api/v1/messages")
                .content(
                    "{\n"
                        + "  \"matchId\": "
                        + match.getId()
                        + ",\n"
                        + "  \"content\": \"content\",\n"
                        + "  \"parentMessageId\": "
                        + message.getId()
                        + "\n"
                        + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$").value(SEND_UNPROCESSABLE_ENTITY_PARENT_MESSAGE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void sendAndExpectBadRequest() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    mockMvc
        .perform(
            post("/api/v1/messages")
                .content(
                    "{\n"
                        + "  \"matchId\": "
                        + match.getId()
                        + ",\n"
                        + "  \"ctnt\": \"content\",\n"
                        + "  \"parentMessageId\": null\n"
                        + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            result -> result.getResponse().getContentAsString().contains("The content is required"))
        .andExpect(jsonPath("$.errors", hasSize(1)));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void sendAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(
            post("/api/v1/messages")
                .content(
                    "{\n"
                        + "  \"matchId\": 1,\n"
                        + "  \"content\": \"content\",\n"
                        + "  \"parentMessageId\": null\n"
                        + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$").value(SEND_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void findMessagesByMatchIdAndExpectOkNoMessages() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    mockMvc
        .perform(get("/api/v1/conversations/{id}", match.getId()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void findMessagesByMatchIdAndExpectOkWithMessages() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = messageRepository.save(new Message(profileId, sentMessageDto));
    mockMvc
        .perform(get("/api/v1/conversations/{id}", match.getId()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("_embedded.messageDtoList[0].id", greaterThan(0)))
        .andExpect(jsonPath("_embedded.messageDtoList[0].senderId").value(profileId))
        .andExpect(
            jsonPath("_embedded.messageDtoList[0].content").value(sentMessageDto.getContent()))
        .andExpect(jsonPath("_embedded.messageDtoList[0].parentMessageId").isEmpty())
        .andExpect(jsonPath("_embedded.messageDtoList[0].senderReaction").isEmpty())
        .andExpect(jsonPath("_embedded.messageDtoList[0].receiverReaction").isEmpty())
        .andExpect(jsonPath("_embedded.messageDtoList[0].status").value(message.getStatus().name()))
        .andExpect(jsonPath("_embedded.messageDtoList[0].seenAt").isEmpty())
        .andExpect(
            jsonPath("_embedded.messageDtoList[0]._links.reply.href")
                .value("http://localhost/api/v1/messages"))
        .andExpect(
            jsonPath(
                "_embedded.messageDtoList[0]._links.['react like'].href",
                matchesRegex("http://localhost/api/v1/messages/\\d+/LIKE")))
        .andExpect(
            jsonPath(
                "_embedded.messageDtoList[0]._links.['react super'].href",
                matchesRegex("http://localhost/api/v1/messages/\\d+/SUPER")))
        .andExpect(
            jsonPath(
                "_embedded.messageDtoList[0]._links.['react haha'].href",
                matchesRegex("http://localhost/api/v1/messages/\\d+/HAHA")))
        .andExpect(
            jsonPath(
                "_embedded.messageDtoList[0]._links.['react cry'].href",
                matchesRegex("http://localhost/api/v1/messages/\\d+/CRY")))
        .andExpect(
            jsonPath(
                "_embedded.messageDtoList[0]._links.['react wrr'].href",
                matchesRegex("http://localhost/api/v1/messages/\\d+/WRR")))
        .andExpect(
            jsonPath(
                "_embedded.messageDtoList[0]._links.['react care'].href",
                matchesRegex("http://localhost/api/v1/messages/\\d+/CARE")))
        .andExpect(
            jsonPath(
                "_embedded.messageDtoList[0]._links.['delete reaction'].href",
                matchesRegex("http://localhost/api/v1/messages/\\d+")));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void findMessagesByMatchIdAndExpectUnprocessableEntityNotValid() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    mockMvc
        .perform(get("/api/v1/conversations/{id}", -1))
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
  void findMessagesByMatchIdAndExpectUnprocessableEntityProfileNotBelongsToMatch()
      throws Exception {
    deleteProfileIfExistsById(anotherId);
    deleteProfileIfExistsById(anotherId2);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    createProfileIfNotExists(anotherId2, anotherProfile2);
    Match match = matchRepository.save(new Match(anotherId, anotherId2));
    mockMvc
        .perform(get("/api/v1/conversations/{id}", match.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$").value(UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void findMessagesByMatchIdAndExpectUnprocessableEntityMatchNotExists() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    matchRepository.deleteById(match.getId());
    mockMvc
        .perform(get("/api/v1/conversations/{id}", match.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$").value(UNPROCESSABLE_ENTITY_MATCH_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void findMessagesByMatchIdAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(get("/api/v1/conversations/{id}", match.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$")
                .value(FIND_MESSAGES_BY_MATCH_ID_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void reactToMessageAndExpectOk() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = messageRepository.save(new Message(profileId, sentMessageDto));
    mockMvc
        .perform(put("/api/v1/messages/{id}/{reaction}", message.getId(), "LIKE"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(message.getId()))
        .andExpect(jsonPath("$.senderId").value(message.getSenderId()))
        .andExpect(jsonPath("$.matchId").value(message.getMatchId()))
        .andExpect(jsonPath("$.content").value(message.getContent()))
        .andExpect(jsonPath("$.parentMessageId").value(message.getParentMessageId()))
        .andExpect(jsonPath("$.senderReaction").value("LIKE"))
        .andExpect(jsonPath("$.receiverReaction").isEmpty())
        .andExpect(jsonPath("$.status").value(message.getStatus().name()))
        .andExpect(jsonPath("$.seenAt").isEmpty())
        .andExpect(jsonPath("$._links.reply.href").value("http://localhost/api/v1/messages"))
        .andExpect(
            jsonPath("$._links.['react like'].href")
                .value("http://localhost/api/v1/messages/" + message.getId() + "/LIKE"))
        .andExpect(
            jsonPath("$._links.['react super'].href")
                .value("http://localhost/api/v1/messages/" + message.getId() + "/SUPER"))
        .andExpect(
            jsonPath("$._links.['react haha'].href")
                .value("http://localhost/api/v1/messages/" + message.getId() + "/HAHA"))
        .andExpect(
            jsonPath("$._links.['react cry'].href")
                .value("http://localhost/api/v1/messages/" + message.getId() + "/CRY"))
        .andExpect(
            jsonPath("$._links.['react wrr'].href")
                .value("http://localhost/api/v1/messages/" + message.getId() + "/WRR"))
        .andExpect(
            jsonPath("$._links.['react care'].href")
                .value("http://localhost/api/v1/messages/" + message.getId() + "/CARE"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void reactToMessageAndExpectUnprocessableEntityNotValid() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(put("/api/v1/messages/{id}/{reaction}", -1, "L"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("Reaction must have value LIKE, SUPER, CARE, HAHA, WOW, CRY or WRR"))
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("The message id must be a positive number"))
        .andExpect(jsonPath("$.errors", hasSize(2)));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void reactToMessageAndExpectUnprocessableEntityMessageNotExists() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = messageRepository.save(new Message(profileId, sentMessageDto));
    messageRepository.deleteById(message.getId());
    mockMvc
        .perform(put("/api/v1/messages/{id}/{reaction}", message.getId(), "LIKE"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$").value(UNPROCESSABLE_ENTITY_MESSAGE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void reactToMessageAndExpectUnprocessableEntityProfileNotBelongsToMatch() throws Exception {
    deleteProfileIfExistsById(anotherId);
    deleteProfileIfExistsById(anotherId2);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    createProfileIfNotExists(anotherId2, anotherProfile2);
    Match match = matchRepository.save(new Match(anotherId, anotherId2));
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = messageRepository.save(new Message(anotherId, sentMessageDto));
    mockMvc
        .perform(put("/api/v1/messages/{id}/{reaction}", message.getId(), "LIKE"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$")
                .value(
                    UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_CONTAINING_GIVEN_MESSAGE_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void reactToMessageAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = messageRepository.save(new Message(profileId, sentMessageDto));
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(put("/api/v1/messages/{id}/{reaction}", message.getId(), "LIKE"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$").value(REACT_TO_MESSAGE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void deleteReactionFromMessageAndExpectOk() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = messageRepository.save(new Message(profileId, sentMessageDto));
    message.setSenderReaction(MessageReaction.LIKE);
    messageRepository.save(message);
    mockMvc
        .perform(put("/api/v1/messages/{id}", message.getId()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(message.getId()))
        .andExpect(jsonPath("$.senderId").value(message.getSenderId()))
        .andExpect(jsonPath("$.matchId").value(message.getMatchId()))
        .andExpect(jsonPath("$.content").value(message.getContent()))
        .andExpect(jsonPath("$.parentMessageId").value(message.getParentMessageId()))
        .andExpect(jsonPath("$.senderReaction").isEmpty())
        .andExpect(jsonPath("$.receiverReaction").isEmpty())
        .andExpect(jsonPath("$.status").value(message.getStatus().name()))
        .andExpect(jsonPath("$.seenAt").isEmpty())
        .andExpect(jsonPath("$._links.reply.href").value("http://localhost/api/v1/messages"))
        .andExpect(
            jsonPath("$._links.['react like'].href")
                .value("http://localhost/api/v1/messages/" + message.getId() + "/LIKE"))
        .andExpect(
            jsonPath("$._links.['react super'].href")
                .value("http://localhost/api/v1/messages/" + message.getId() + "/SUPER"))
        .andExpect(
            jsonPath("$._links.['react haha'].href")
                .value("http://localhost/api/v1/messages/" + message.getId() + "/HAHA"))
        .andExpect(
            jsonPath("$._links.['react cry'].href")
                .value("http://localhost/api/v1/messages/" + message.getId() + "/CRY"))
        .andExpect(
            jsonPath("$._links.['react wrr'].href")
                .value("http://localhost/api/v1/messages/" + message.getId() + "/WRR"))
        .andExpect(
            jsonPath("$._links.['react care'].href")
                .value("http://localhost/api/v1/messages/" + message.getId() + "/CARE"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void deleteReactionFromMessageAndExpectUnprocessableEntityNotValid() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(put("/api/v1/messages/{id}", -1))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("The message id must be a positive number"))
        .andExpect(jsonPath("$.errors", hasSize(1)));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void deleteReactionFromMessageAndExpectUnprocessableEntityMessageNotExists() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = messageRepository.save(new Message(profileId, sentMessageDto));
    messageRepository.deleteById(message.getId());
    mockMvc
        .perform(put("/api/v1/messages/{id}", message.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$").value(UNPROCESSABLE_ENTITY_MESSAGE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void deleteReactionFromMessageAndExpectUnprocessableEntityProfileNotBelongsToMatch()
      throws Exception {
    deleteProfileIfExistsById(anotherId);
    deleteProfileIfExistsById(anotherId2);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    createProfileIfNotExists(anotherId2, anotherProfile2);
    Match match = matchRepository.save(new Match(anotherId, anotherId2));
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = messageRepository.save(new Message(anotherId, sentMessageDto));
    mockMvc
        .perform(put("/api/v1/messages/{id}", message.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$")
                .value(
                    UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_CONTAINING_GIVEN_MESSAGE_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void deleteReactionFromMessageAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = messageRepository.save(new Message(profileId, sentMessageDto));
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(put("/api/v1/messages/{id}", message.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$")
                .value(
                    DELETE_REACTION_FROM_MESSAGE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void readMessagesInConversationAndExceptOk() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = messageRepository.save(new Message(profileId, sentMessageDto));
    mockMvc
        .perform(put("/api/v1/messages/read/{matchId}", match.getId()))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void readMessagesInConversationAndExceptUnprocessableEntityNotValid() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(profileId, anotherId));
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = messageRepository.save(new Message(profileId, sentMessageDto));
    mockMvc
        .perform(put("/api/v1/messages/read/{matchId}", -1))
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
  void readMessagesInConversationAndExceptUnprocessableEntityProfileNotBelongToMatch()
      throws Exception {
    deleteProfileIfExistsById(anotherId);
    deleteProfileIfExistsById(anotherId2);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(anotherId, anotherId2));
    mockMvc
        .perform(put("/api/v1/messages/read/{matchId}", match.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$").value(UNPROCESSABLE_ENTITY_PROFILE_NOT_BELONGS_TO_MATCH_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void readMessagesInConversationAndExceptUnprocessableEntityMatchNotExists() throws Exception {
    deleteProfileIfExistsById(anotherId);
    deleteProfileIfExistsById(anotherId2);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(anotherId, anotherId2));
    matchRepository.deleteById(match.getId());
    mockMvc
        .perform(put("/api/v1/messages/read/{matchId}", match.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$").value(UNPROCESSABLE_ENTITY_MATCH_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void readMessagesInConversationAndExceptUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(anotherId);
    deleteProfileIfExistsById(anotherId2);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    Match match = matchRepository.save(new Match(anotherId, anotherId2));
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(put("/api/v1/messages/read/{matchId}", match.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$")
                .value(
                    READ_MESSAGES_IN_CONVERSATION_AND_EXCEPT_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void setMessagesStatusAsDeliveredAndExpectOk() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(put("/api/v1/messages/setAsDelivered"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void setMessagesStatusAsDeliveredAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(put("/api/v1/messages/setAsDelivered"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$")
                .value(
                    SET_MESSAGES_STATUS_AS_DELIVERED_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }
}
