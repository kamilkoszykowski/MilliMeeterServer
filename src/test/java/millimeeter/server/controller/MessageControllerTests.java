package millimeeter.server.controller;

import millimeeter.server.dto.SentMessageDto;
import millimeeter.server.enums.MessageReaction;
import millimeeter.server.model.Match;
import millimeeter.server.model.Message;
import millimeeter.server.model.Profile;
import millimeeter.server.repository.MatchRepository;
import millimeeter.server.repository.MessageRepository;
import millimeeter.server.service.TestUtils;
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

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.matchesRegex;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MessageControllerTests {

  private final MatchRepository matchRepository;
  private final MessageRepository messageRepository;
  private final TestUtils testUtils;
  private final WebApplicationContext applicationContext;
  private MockMvc mockMvc;

  @Autowired
  public MessageControllerTests(
      MatchRepository matchRepository,
      MessageRepository messageRepository,
      TestUtils testUtils,
      WebApplicationContext applicationContext) {
    this.matchRepository = matchRepository;
    this.messageRepository = messageRepository;
    this.testUtils = testUtils;
    this.applicationContext = applicationContext;
  }

  static final String USERNAME = TestUtils.USERNAME;
  private Profile profile;
  private Profile anotherProfile;
  private Profile anotherProfile2;

  @BeforeEach
  void init() {
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(applicationContext)
            .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
            .build();
    testUtils.createUserIfNotExists(TestUtils.USERNAME, testUtils.getProfile());
    testUtils.createUserIfNotExists(TestUtils.ANOTHER_USERNAME, testUtils.getAnotherProfile());
    testUtils.createUserIfNotExists(TestUtils.ANOTHER_USERNAME_2, testUtils.getAnotherProfile2());
    testUtils.createProfileIfNotExists(testUtils.getProfile());
    testUtils.deleteProfileIfExistsById(testUtils.getAnotherProfile());
    testUtils.createProfileIfNotExists(testUtils.getAnotherProfile());
    profile = testUtils.getProfile();
    anotherProfile = testUtils.getAnotherProfile();
    anotherProfile2 = testUtils.getAnotherProfile2();
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyValidSendRequestReturnCreated() throws Exception {
    Match match = testUtils.addMatch(profile.getId(), anotherProfile.getId());
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
        .andExpect(jsonPath("$.senderId").value(profile.getId()))
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
  void verifySendingMessageNotBelongingToMatchReturnUnprocessableEntity() throws Exception {
    testUtils.deleteProfileIfExistsById(anotherProfile2);
    testUtils.createProfileIfNotExists(anotherProfile2);
    Match match = testUtils.addMatch(anotherProfile.getId(), anotherProfile2.getId());
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
        .andExpect(jsonPath("$.errors").value("Profile not belongs to match"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifySendingMessageInNotExistingMessageReturnUnprocessableEntity() throws Exception {
    Match match = testUtils.addMatch(profile.getId(), anotherProfile.getId());
    testUtils.deleteProfileIfExistsById(anotherProfile);
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
        .andExpect(jsonPath("$.errors").value("Match not exists"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyReplyingToNotExistingMessageReturnUnprocessableEntity() throws Exception {
    Match match = testUtils.addMatch(profile.getId(), anotherProfile.getId());
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = testUtils.addMessage(profile.getId(), sentMessageDto);
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
        .andExpect(jsonPath("$.errors").value("Parent message not exists"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyValidFindMessagesFromMatchReturnOkNoMessages() throws Exception {
    Match match = testUtils.addMatch(profile.getId(), anotherProfile.getId());
    mockMvc
        .perform(get("/api/v1/conversations/{id}", match.getId()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyValidFindMessagesFromMatchReturnOkWithMessages() throws Exception {
    Match match = testUtils.addMatch(profile.getId(), anotherProfile.getId());
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = testUtils.addMessage(profile.getId(), sentMessageDto);
    mockMvc
        .perform(get("/api/v1/conversations/{id}", match.getId()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("_embedded.messageDtoList[0].id", greaterThan(0)))
        .andExpect(jsonPath("_embedded.messageDtoList[0].senderId").value(profile.getId()))
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
  void verifyFindingMessagesByProfileNotBelongingToMatchReturnsUnprocessableEntity()
      throws Exception {
    testUtils.deleteProfileIfExistsById(anotherProfile2);
    testUtils.createProfileIfNotExists(anotherProfile2);
    Match match = testUtils.addMatch(anotherProfile.getId(), anotherProfile2.getId());
    mockMvc
        .perform(get("/api/v1/conversations/{id}", match.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors").value("Profile not belongs to match"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyFindingMatchesFromNotExistingMatchReturnUnprocessableEntity() throws Exception {
    Match match = testUtils.addMatch(profile.getId(), anotherProfile.getId());
    matchRepository.deleteById(match.getId());
    mockMvc
        .perform(get("/api/v1/conversations/{id}", match.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors").value("Match not exists"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyReactingToMessageReturnOk() throws Exception {
    Match match = testUtils.addMatch(profile.getId(), anotherProfile.getId());
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = testUtils.addMessage(profile.getId(), sentMessageDto);
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
  void verifyReactingToNotExistingMessageReturnUnprocessableEntity() throws Exception {
    Match match = testUtils.addMatch(profile.getId(), anotherProfile.getId());
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = testUtils.addMessage(profile.getId(), sentMessageDto);
    messageRepository.deleteById(message.getId());
    mockMvc
        .perform(put("/api/v1/messages/{id}/{reaction}", message.getId(), "LIKE"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors").value("Message not exists"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyReactingToMessageNotBelongingToProfileMatchesReturnUnprocessableEntity() throws Exception {
    testUtils.deleteProfileIfExistsById(anotherProfile2);
    testUtils.createProfileIfNotExists(anotherProfile2);
    Match match = testUtils.addMatch(anotherProfile.getId(), anotherProfile2.getId());
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = testUtils.addMessage(anotherProfile.getId(), sentMessageDto);
    mockMvc
        .perform(put("/api/v1/messages/{id}/{reaction}", message.getId(), "LIKE"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$.errors").value("Profile not belongs to match containing given message"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyDeletingReactionFromMessageReturnOk() throws Exception {
    Match match = testUtils.addMatch(profile.getId(), anotherProfile.getId());
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = testUtils.addMessage(profile.getId(), sentMessageDto);
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
  void verifyDeletingReactionFromNotExistingMessageReturnUnprocessableEntity() throws Exception {
    Match match = testUtils.addMatch(profile.getId(), anotherProfile.getId());
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = testUtils.addMessage(profile.getId(), sentMessageDto);
    messageRepository.deleteById(message.getId());
    mockMvc
        .perform(put("/api/v1/messages/{id}", message.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors").value("Message not exists"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyDeletingReactionFromMessageByProfileNotBelongingToMatchReturnUnprocessableEntity()
      throws Exception {
    testUtils.deleteProfileIfExistsById(anotherProfile2);
    testUtils.createProfileIfNotExists(anotherProfile2);
    Match match = testUtils.addMatch(anotherProfile.getId(), anotherProfile2.getId());
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = testUtils.addMessage(anotherProfile.getId(), sentMessageDto);
    mockMvc
        .perform(put("/api/v1/messages/{id}", message.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$.errors").value("Profile not belongs to match containing given message"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyReadingMessagesInConversationReturnOk() throws Exception {
    Match match = testUtils.addMatch(profile.getId(), anotherProfile.getId());
    SentMessageDto sentMessageDto = new SentMessageDto(match.getId(), "content", null);
    Message message = testUtils.addMessage(profile.getId(), sentMessageDto);
    mockMvc
        .perform(put("/api/v1/messages/read/{matchId}", match.getId()))
        .andDo(print())
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyReadingMessagesInConversationByProfileNotBelongingToMatchReturnUnprocessableEntity()
      throws Exception {
    testUtils.deleteProfileIfExistsById(anotherProfile2);
    testUtils.createProfileIfNotExists(anotherProfile2);
    Match match = testUtils.addMatch(anotherProfile.getId(), anotherProfile2.getId());
    mockMvc
        .perform(put("/api/v1/messages/read/{matchId}", match.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors").value("Profile not belongs to match"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyReadingMessagesFromNotExistingMatchReturnUnprocessableEntity() throws Exception {
    testUtils.deleteProfileIfExistsById(anotherProfile2);
    testUtils.createProfileIfNotExists(anotherProfile2);
    Match match = testUtils.addMatch(anotherProfile.getId(), anotherProfile2.getId());
    matchRepository.deleteById(match.getId());
    mockMvc
        .perform(put("/api/v1/messages/read/{matchId}", match.getId()))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors").value("Match not exists"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifySettingMessagesStatusAsDeliveredReturnOk() throws Exception {
    mockMvc
        .perform(put("/api/v1/messages/setAsDelivered"))
        .andDo(print())
        .andExpect(status().isNoContent());
  }
}
