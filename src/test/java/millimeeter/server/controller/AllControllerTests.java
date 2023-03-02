package millimeeter.server.controller;

import millimeeter.server.repository.UserRepository;
import millimeeter.server.service.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
    TO MAKE TEST CLASSES FOR EVERY CONTROLLER CLEANER AND SMALLER,
    TESTS FOR NOT EXISTING USER, NOT EXISTING PROFILE, INVALID REQUESTS AND BAD REQUESTS
    ARE PLACED HERE AND COMBINED INTO PARAMETRIZED TESTS
*/

@SpringBootTest
@AutoConfigureMockMvc
class AllControllerTests {

  private final UserRepository userRepository;
  private final TestUtils testUtils;
  private final WebApplicationContext applicationContext;
  private MockMvc mockMvc;

  @Autowired
  public AllControllerTests(
      UserRepository userRepository,
      TestUtils testUtils,
      WebApplicationContext applicationContext) {
    this.userRepository = userRepository;
    this.testUtils = testUtils;
    this.applicationContext = applicationContext;
  }

  static final String USERNAME = TestUtils.USERNAME;

  @BeforeEach
  void init() {
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(applicationContext)
            .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
            .build();
    testUtils.createUserIfNotExists(TestUtils.USERNAME, testUtils.getProfile());
  }

  /*
    REQUEST TESTS

    PARAMETERS HAVE THE FOLLOWING FORMAT:
    "methodName; path; content;"
  */

  @ParameterizedTest
  @ValueSource(
      strings = {
        "GET; /api/v1/conversations/a",
        "POST; /api/v1/messages; {\"m\": -1, \"content\": \"Message content\","
            + " \"parentMessageId\": \"\"}",
        "POST; /api/v1/swipes/a/L",
        "PUT; /api/v1/messages/read/a",
        "PUT; /api/v1/messages/a",
        "PUT; /api/v1/messages/a/L",
        "PUT; /api/v1/profiles; {\"description\": \"Updated description\", \"mySong\":"
            + " \"newMySong\", \"l\": 999.99, \"lastLongitude\": -181.0, \"lookingFor\":"
            + " \"GIRLS\", \"searchDistance\": 101, \"ageRangeMinimum\": 12, \"ageRangeMaximum\":"
            + " 14}",
        "PUT; /api/v1/profiles/location; {\"l\": 999.99, \"lastLongitude\": -181.0}",
        "PUT; /api/v1/profiles/photos/a",
        "DELETE; /api/v1/matches/a",
      })
  @WithMockUser(username = USERNAME)
  void verifyBadRequestReturnBadRequest(String endpoints) throws Exception {
    testUtils.createProfileIfNotExists(testUtils.getProfile());
    String[] inputData = endpoints.split(";\\s*");
    MockHttpServletRequestBuilder builder = createRequestBuilder(inputData);
    mockMvc.perform(builder).andDo(print()).andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "GET; /api/v1/conversations/-1",
        "POST; /api/v1/messages; {\"matchId\": -1, \"content\": \"Message content\","
            + " \"parentMessageId\": \"\"}",
        "POST; /api/v1/swipes/-1/L",
        "PUT; /api/v1/messages/read/-1",
        "PUT; /api/v1/messages/-1",
        "PUT; /api/v1/messages/-1/L",
        "PUT; /api/v1/profiles; {\"description\": \"Updated description\", \"mySong\":"
            + " \"newMySong\", \"lastLatitude\": 999.99, \"lastLongitude\": -181.0, \"lookingFor\":"
            + " \"GIRLS\", \"searchDistance\": 101, \"ageRangeMinimum\": 12, \"ageRangeMaximum\":"
            + " 14}",
        "PUT; /api/v1/profiles/location; {\"lastLatitude\": 999.99, \"lastLongitude\": -181.0}",
        "PUT; /api/v1/profiles/photos/-1",
        "DELETE; /api/v1/matches/-1",
      })
  @WithMockUser(username = USERNAME)
  void verifyNotValidRequestReturnNotValid(String endpoints) throws Exception {
    testUtils.createProfileIfNotExists(testUtils.getProfile());
    String[] inputData = endpoints.split(";\\s*");
    MockHttpServletRequestBuilder builder = createRequestBuilder(inputData);
    mockMvc
        .perform(builder)
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors.size()", greaterThanOrEqualTo(1)))
        .andExpect(result -> result.getResponse().getContentAsString().contains("must"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "GET; /api/v1/conversations",
        "GET; /api/v1/conversations/1",
        "GET; /api/v1/matches",
        "GET; /api/v1/profiles",
        "GET; /api/v1/profiles/me",
        "GET; /api/v1/profiles/swipesLeft",
        "POST; /api/v1/messages; {\"matchId\": 1, \"content\": \"Message content\","
            + " \"parentMessageId\": \"\"}",
        "POST; /api/v1/swipes/1/LEFT",
        "PUT; /api/v1/messages/read/1",
        "PUT; /api/v1/messages/setAsDelivered",
        "PUT; /api/v1/messages/1",
        "PUT; /api/v1/messages/1/LIKE",
        "PUT; /api/v1/profiles; {\"description\": \"Updated description\", \"mySong\":"
            + " \"newMySong\", \"lastLatitude\": 0.0, \"lastLongitude\": 0.0, \"lookingFor\":"
            + " \"WOMEN\", \"searchDistance\": 69, \"ageRangeMinimum\": 18, \"ageRangeMaximum\":"
            + " 30}",
        "PUT; /api/v1/profiles/location; {\"lastLatitude\": 0.0, \"lastLongitude\": 0.0}",
        "PUT; /api/v1/profiles/photos/1",
        "DELETE; /api/v1/matches/1",
        "DELETE; /api/v1/profiles"
      })
  @WithMockUser(username = USERNAME)
  void verifyRequestsWithoutExistingUserReturnUserNotExists(String endpoints)
      throws Exception {
    userRepository.deleteById(USERNAME);
    String[] inputData = endpoints.split(";\\s*");
    MockHttpServletRequestBuilder builder = createRequestBuilder(inputData);
    mockMvc
        .perform(builder)
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0]").value("User not exists"))
        .andExpect(jsonPath("$.errors", hasSize(1)));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "GET; /api/v1/conversations",
        "GET; /api/v1/conversations/1",
        "GET; /api/v1/matches",
        "GET; /api/v1/profiles",
        "GET; /api/v1/profiles/me",
        "GET; /api/v1/profiles/swipesLeft",
        "POST; /api/v1/messages; {\"matchId\": 1, \"content\": \"Message content\","
            + " \"parentMessageId\": \"\"}",
        "POST; /api/v1/swipes/1/LEFT",
        "PUT; /api/v1/messages/read/1",
        "PUT; /api/v1/messages/setAsDelivered",
        "PUT; /api/v1/messages/1",
        "PUT; /api/v1/messages/1/LIKE",
        "PUT; /api/v1/profiles; {\"description\": \"Updated description\", \"mySong\":"
            + " \"newMySong\", \"lastLatitude\": 0.0, \"lastLongitude\": 0.0, \"lookingFor\":"
            + " \"WOMEN\", \"searchDistance\": 69, \"ageRangeMinimum\": 18, \"ageRangeMaximum\":"
            + " 30}",
        "PUT; /api/v1/profiles/location; {\"lastLatitude\": 0.0, \"lastLongitude\": 0.0}",
        "PUT; /api/v1/profiles/photos/1",
        "DELETE; /api/v1/matches/1"
      })
  @WithMockUser(username = USERNAME)
  void verifyRequestsWithoutExistingProfileReturnProfileNotExistsError(String endpoints)
      throws Exception {
    testUtils.deleteProfileIfExistsById(testUtils.getProfile());
    String[] inputData = endpoints.split(";\\s*");
    MockHttpServletRequestBuilder builder = createRequestBuilder(inputData);
    mockMvc
        .perform(builder)
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0]").value("Profile not exists"))
        .andExpect(jsonPath("$.errors", hasSize(1)));
  }

  /*
    MULTIPART REQUEST TESTS

    PARAMETERS HAVE THE FOLLOWING FORMAT:
    "methodName; path; content; photosAmount; registrationJsonAmount;"
  */

  @ParameterizedTest
  @ValueSource(strings = {"POST; /api/v1/profiles", "PUT; /api/v1/profiles/photos?index=5"})
  @WithMockUser(username = USERNAME)
  void verifyBadMultipartRequestReturnBadRequest(String endpoints) throws Exception {
    testUtils.createProfileIfNotExists(testUtils.getProfile());
    String[] inputData = endpoints.split(";\\s*");
    MockMultipartHttpServletRequestBuilder builder = createMultipartRequestBuilder(inputData);
    mockMvc.perform(builder).andDo(print()).andExpect(status().isBadRequest());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"POST; /api/v1/profiles;; 6; 1", "PUT; /api/v1/profiles/photos?index=5;; 1"})
  @WithMockUser(username = USERNAME)
  void verifyNotValidMultipartRequestReturnNotValid(String endpoints) throws Exception {
    if (endpoints.contains("POST; /api/v1/profiles;")) { // profile can not exist before creating via request
      testUtils.deleteProfileIfExistsById(testUtils.getProfile());
    } else {
      testUtils.createProfileIfNotExists(testUtils.getProfile());
    }
    String[] inputData = endpoints.split(";\\s*");
    MockMultipartHttpServletRequestBuilder builder = createMultipartRequestBuilder(inputData);
    mockMvc
        .perform(builder)
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors.size()", greaterThanOrEqualTo(1)))
        .andExpect(result -> result.getResponse().getContentAsString().contains("must"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"POST; /api/v1/profiles;; 5; 1", "PUT; /api/v1/profiles/photos?index=0;; 1"})
  @WithMockUser(username = USERNAME)
  void verifyMultipartRequestsWithoutExistingUserReturnUserNotExists(String endpoints)
      throws Exception {
    userRepository.deleteById(USERNAME);
    String[] inputData = endpoints.split(";\\s*");
    MockMultipartHttpServletRequestBuilder builder = createMultipartRequestBuilder(inputData);
    mockMvc
        .perform(builder)
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0]").value("User not exists"))
        .andExpect(jsonPath("$.errors", hasSize(1)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"PUT; /api/v1/profiles/photos?index=0;; 1"})
  @WithMockUser(username = USERNAME)
  void verifyMultipartRequestsWithoutExistingProfileReturnProfileNotExists(String endpoints)
      throws Exception {
    testUtils.deleteProfileIfExistsById(testUtils.getProfile());
    String[] inputData = endpoints.split(";\\s*");
    MockMultipartHttpServletRequestBuilder builder = createMultipartRequestBuilder(inputData);
    mockMvc
        .perform(builder)
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors[0]").value("Profile not exists"))
        .andExpect(jsonPath("$.errors", hasSize(1)));
  }

  MockHttpServletRequestBuilder createRequestBuilder(String[] inputData) {
    String method = inputData[0];
    String path = inputData[1];
    String body = "";
    if (inputData.length == 3) {
      body = inputData[2];
    }

    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(path);
    builder.with(createRequestPostProcessor(method, body));

    return builder;
  }

  MockMultipartHttpServletRequestBuilder createMultipartRequestBuilder(String[] inputData) {
    String method = inputData[0];
    String path = inputData[1];
    String body = "";
    int photoAmount = 0;
    int bodyAmount = 0;
    if (inputData.length >= 3) {
      body = inputData[2];
    }
    if (inputData.length >= 4) {
      photoAmount = Integer.parseInt(inputData[3]);
    }
    if (inputData.length == 5) {
      bodyAmount = Integer.parseInt(inputData[4]);
    }

    MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(path);
    builder.with(createRequestPostProcessor(method, body));

    for (int i = 0; i < photoAmount; i++) {
      builder = builder.file(testUtils.getPhoto());
    }
    for (int i = 0; i < bodyAmount; i++) {
      builder = builder.file(testUtils.getBody());
    }

    return builder;
  }

  RequestPostProcessor createRequestPostProcessor(String method, String body) {
    return new RequestPostProcessor() {
      @Override
      public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
        switch (method) {
          case "GET" -> request.setMethod(method);
          case "POST" -> request.setMethod(method);
          case "PUT" -> request.setMethod(method);
          case "DELETE" -> request.setMethod(method);
          default -> request.setMethod("GET");
        }
        if (!body.isEmpty()) {
          request.setContentType(MediaType.APPLICATION_JSON_VALUE);
          request.setContent(body.getBytes());
        }
        return request;
      }
    };
  }
}
