package millimeeter.server.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import millimeeter.server.repository.UserRepository;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.AfterAll;
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
class UserControllerTests {

  private final UserRepository userRepository;
  private final WebApplicationContext applicationContext;
  private MockMvc mockMvc;

  @Autowired
  public UserControllerTests(
      UserRepository userRepository, WebApplicationContext applicationContext) {
    this.userRepository = userRepository;
    this.applicationContext = applicationContext;
  }

  static final String USERNAME = MessageControllerTests.USERNAME;

  @BeforeEach
  void init() {
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(applicationContext)
            .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
            .build();
    if (userRepository.findById(USERNAME).isPresent()) {
      userRepository.deleteById(USERNAME);
    }
  }

  @AfterAll
  static void afterAll() {}

  @Test
  @WithMockUser(username = USERNAME)
  void createUserAndExpectCreated() throws Exception {
    mockMvc
        .perform(post("/api/v1/users"))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(USERNAME))
        .andExpect(jsonPath("$.profileId").value(greaterThan(0)))
        .andExpect(
            jsonPath("$._links.['create profile'].href").value("http://localhost/api/v1/profiles"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void createUserAndExpectConflict() throws Exception {
    userRepository.createUser(USERNAME);
    mockMvc
        .perform(post("/api/v1/users"))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors").value("User already exists"));
  }

  @Test
  void zDeleteUsersAfterAllTests() throws Exception {
    String[] usernames = {"mockUser", "anotherUser", "anotherUser2"};
    String[] photos = {
      "photo1", "photo2", "anotherPhoto1", "anotherPhoto2", "anotherPhoto2_1", "anotherPhoto2_2"
    };
    for (String username : usernames) {
      if (userRepository.findById(username).isPresent()) {
        userRepository.deleteById(username);
      }
    }
    for (String photo : photos) {
      if (FileUtils.fileExists("photos/" + photo + ".jpg")) {
        FileUtils.fileDelete("photos/" + photo + ".jpg");
      }
    }
  }
}
