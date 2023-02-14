package millimeeter.server.controller;

import static millimeeter.server.controller.response.ProfileControllerResponses.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import millimeeter.server.dto.RegistrationDto;
import millimeeter.server.enums.Gender;
import millimeeter.server.enums.LookingFor;
import millimeeter.server.model.Profile;
import millimeeter.server.repository.ProfileRepository;
import millimeeter.server.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTests {

  @Autowired UserRepository userRepository;
  @Autowired ProfileRepository profileRepository;
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

  static RegistrationDto registrationDto =
      new RegistrationDto(
          "Phhjdnjuahunyksfu",
          LocalDate.parse("2000-01-31"),
          "WOMAN",
          "string",
          "9QJm7ECVsC0Lygh33KbiwdqCvl5txHfVJERBtMYO6kEvOfRA71slgcb6Kc3Fu7KQxFX9LD4KO",
          90.0,
          90.0,
          "WOMEN",
          100,
          100,
          100);

  static String bodyJson =
      "{\n"
          + "    \"firstName\": \""
          + registrationDto.getFirstName()
          + "\",\n"
          + "    \"dateOfBirth\": \""
          + registrationDto.getDateOfBirth()
          + "\",\n"
          + "    \"gender\": \""
          + registrationDto.getGender()
          + "\",\n"
          + "    \"description\": \""
          + registrationDto.getDescription()
          + "\",\n"
          + "    \"mySong\": \""
          + registrationDto.getMySong()
          + "\",\n"
          + "    \"lastLatitude\": "
          + registrationDto.getLastLatitude()
          + ",\n"
          + "    \"lastLongitude\": "
          + registrationDto.getLastLongitude()
          + ",\n"
          + "    \"lookingFor\": \""
          + registrationDto.getLookingFor()
          + "\",\n"
          + "    \"searchDistance\": "
          + registrationDto.getSearchDistance()
          + ",\n"
          + "    \"ageRangeMinimum\": "
          + registrationDto.getAgeRangeMinimum()
          + ",\n"
          + "    \"ageRangeMaximum\": "
          + registrationDto.getAgeRangeMaximum()
          + "\n"
          + "}";

  static final String TEST_PHOTO_PATH = "src/main/resources/static/testPhoto.jpg";

  static MockMultipartFile photo =
      createMockFile("photos", "magik.jpg", MediaType.IMAGE_JPEG_VALUE, TEST_PHOTO_PATH);
  static MockMultipartFile body =
      createMockFile("body", "test.json", MediaType.APPLICATION_JSON_VALUE, bodyJson);

  static MockMultipartFile createMockFile(
      String name, String originalFilename, String mediaType, String path) {
    try {
      return new MockMultipartFile(
          name,
          originalFilename,
          mediaType,
          mediaType.equals("application/json")
              ? path.getBytes()
              : new FileInputStream(path).readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void createProfileIfNotExists(Long profileId, Profile profile) {
    try {
      FileOutputStream output = new FileOutputStream("photos/photo1.jpg");
      output.write(new FileInputStream(TEST_PHOTO_PATH).readAllBytes());
      output.close();
      FileOutputStream output2 = new FileOutputStream("photos/photo2.jpg");
      output2.write(new FileInputStream(TEST_PHOTO_PATH).readAllBytes());
      output2.close();
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
  void findProfilesToSwipeAndExpectOkNoProfiles() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(get("/api/v1/profiles"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void findProfilesToSwipeAndExpectOkWithProfiles() throws Exception {
    deleteProfileIfExistsById(anotherId);
    createProfileIfNotExists(profileId, profile);
    createProfileIfNotExists(anotherId, anotherProfile);
    mockMvc
        .perform(get("/api/v1/profiles"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.profileToSwipeDtoList.size()", greaterThan(0)))
        .andExpect(
            jsonPath("$._embedded.profileToSwipeDtoList[?(@.id == " + anotherId + ")]", hasSize(1)))
        .andExpect(
            jsonPath("$._embedded.profileToSwipeDtoList[?(@.id == " + anotherId + ")].firstName")
                .value(anotherProfile.getFirstName()))
        .andExpect(
            jsonPath("$._embedded.profileToSwipeDtoList[?(@.id == " + anotherId + ")].age")
                .value(
                    LocalDateTime.now()
                        .minusYears(anotherProfile.getDateOfBirth().getYear())
                        .getYear()))
        .andExpect(
            jsonPath("$._embedded.profileToSwipeDtoList[?(@.id == " + anotherId + ")].gender")
                .value(anotherProfile.getGender().name()))
        .andExpect(
            jsonPath("$._embedded.profileToSwipeDtoList[?(@.id == " + anotherId + ")].description")
                .value(anotherProfile.getDescription()))
        .andExpect(
            jsonPath("$._embedded.profileToSwipeDtoList[?(@.id == " + anotherId + ")].mySong")
                .value(anotherProfile.getMySong()))
        .andExpect(
            jsonPath(
                    "$._embedded.profileToSwipeDtoList[?(@.id == "
                        + anotherId
                        + " && @.distanceAway <= 100)]")
                .exists())
        .andExpect(
            jsonPath(
                    "$._embedded.profileToSwipeDtoList[?(@.id == "
                        + anotherId
                        + " && @.photos.size() == 2)]")
                .exists())
        .andExpect(
            jsonPath(
                    "$._embedded.profileToSwipeDtoList[?(@.id == "
                        + anotherId
                        + ")]._links.['swipe left'].href")
                .value("http://localhost/api/v1/swipes/" + anotherId + "/LEFT"))
        .andExpect(
            jsonPath(
                    "$._embedded.profileToSwipeDtoList[?(@.id == "
                        + anotherId
                        + ")]._links.['swipe right'].href")
                .value("http://localhost/api/v1/swipes/" + anotherId + "/RIGHT"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void findProfilesToSwipeAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(get("/api/v1/profiles"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$")
                .value(FIND_PROFILES_TO_SWIPE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void getMyProfileAndExpectOk() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(get("/api/v1/profiles/me"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value(profile.getFirstName()))
        .andExpect(jsonPath("$.gender").value(profile.getGender().name()))
        .andExpect(jsonPath("$.photos", hasSize(profile.getPhotos().size())))
        .andExpect(jsonPath("$.description").value(profile.getDescription()))
        .andExpect(jsonPath("$.mySong").value(profile.getMySong()))
        .andExpect(jsonPath("$.lookingFor").value(profile.getLookingFor().name()))
        .andExpect(jsonPath("$.searchDistance").value(profile.getSearchDistance()))
        .andExpect(jsonPath("$.ageRangeMinimum").value(profile.getAgeRangeMinimum()))
        .andExpect(jsonPath("$.ageRangeMaximum").value(profile.getAgeRangeMaximum()))
        .andExpect(jsonPath("$._links.update.href").value("http://localhost/api/v1/profiles"))
        .andExpect(jsonPath("$._links.delete.href").value("http://localhost/api/v1/profiles"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void getMyProfileAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(get("/api/v1/profiles/me"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$").value(GET_MY_PROFILE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void createAndExpectCreated() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(
            multipart("/api/v1/profiles")
                .file(photo)
                .file(photo)
                .file(photo)
                .file(photo)
                .file(photo)
                .file(body)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.firstName").value(registrationDto.getFirstName()))
        .andExpect(jsonPath("$.gender").value(registrationDto.getGender()))
        .andExpect(jsonPath("$.photos", hasSize(5)))
        .andExpect(jsonPath("$.description").value(registrationDto.getDescription()))
        .andExpect(jsonPath("$.mySong").value(registrationDto.getMySong()))
        .andExpect(jsonPath("$.lastLatitude").value(registrationDto.getLastLatitude()))
        .andExpect(jsonPath("$.lastLongitude").value(registrationDto.getLastLongitude()))
        .andExpect(jsonPath("$.swipesLeft").value(50))
        .andExpect(jsonPath("$.waitUntil").isEmpty())
        .andExpect(jsonPath("$.lookingFor").value(registrationDto.getLookingFor()))
        .andExpect(jsonPath("$.searchDistance").value(registrationDto.getSearchDistance()))
        .andExpect(jsonPath("$.ageRangeMinimum").value(registrationDto.getAgeRangeMinimum()))
        .andExpect(jsonPath("$.ageRangeMaximum").value(registrationDto.getAgeRangeMaximum()))
        .andExpect(jsonPath("$._links.update.href").value("http://localhost/api/v1/profiles"))
        .andExpect(jsonPath("$._links.delete.href").value("http://localhost/api/v1/profiles"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void createAndExpectConflict() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(
            multipart("/api/v1/profiles").file(photo).file(body).accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$").value(CREATE_CONFLICT_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void createAndExpectUnprocessableEntityTooManyPhotos() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(
            multipart("/api/v1/profiles")
                .file(photo)
                .file(photo)
                .file(photo)
                .file(photo)
                .file(photo)
                .file(photo)
                .file(body)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$").value(CREATE_UNPROCESSABLE_ENTITY_TOO_MANY_PHOTOS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void updateProfileAndExpectOk() throws Exception {
    deleteProfileIfExistsById(profileId);
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(
            put("/api/v1/profiles")
                .content(
                    "{\n"
                        + "  \"description\": \"string\",\n"
                        + "  \"mySong\":"
                        + " \"8CRUfdqO9fpirMnEMqNYbHsWsSS9UO7nySTaa1e2pf5cbuWgwH307TrCBwX07N\",\n"
                        + "  \"lastLatitude\": 1,\n"
                        + "  \"lastLongitude\": 1,\n"
                        + "  \"lookingFor\": \"WOMEN\",\n"
                        + "  \"searchDistance\": 100,\n"
                        + "  \"ageRangeMinimum\": 18,\n"
                        + "  \"ageRangeMaximum\": 100\n"
                        + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(greaterThan(0)))
        .andExpect(jsonPath("$.firstName").value(profile.getFirstName()))
        .andExpect(jsonPath("$.gender").value(profile.getGender().name()))
        .andExpect(jsonPath("$.photos", hasSize(profile.getPhotos().size())))
        .andExpect(jsonPath("$.description").value("string"))
        .andExpect(
            jsonPath("$.mySong")
                .value("8CRUfdqO9fpirMnEMqNYbHsWsSS9UO7nySTaa1e2pf5cbuWgwH307TrCBwX07N"))
        .andExpect(jsonPath("$.lastLatitude").value(1))
        .andExpect(jsonPath("$.lastLongitude").value(1))
        .andExpect(jsonPath("$.swipesLeft").value(profile.getSwipesLeft()))
        .andExpect(jsonPath("$.waitUntil").value(profile.getWaitUntil()))
        .andExpect(jsonPath("$.lookingFor").value("WOMEN"))
        .andExpect(jsonPath("$.searchDistance").value(100))
        .andExpect(jsonPath("$.ageRangeMinimum").value(18))
        .andExpect(jsonPath("$.ageRangeMaximum").value(100))
        .andExpect(jsonPath("$._links.update.href").value("http://localhost/api/v1/profiles"))
        .andExpect(jsonPath("$._links.delete.href").value("http://localhost/api/v1/profiles"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void updateProfileAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(
            put("/api/v1/profiles")
                .content(
                    "{\n"
                        + "  \"description\": \"string\",\n"
                        + "  \"mySong\":"
                        + " \"8CRUfdqO9fpirMnEMqNYbHsWsSS9UO7nySTaa1e2pf5cbuWgwH307TrCBwX07N\",\n"
                        + "  \"lastLatitude\": 1,\n"
                        + "  \"lastLongitude\": 1,\n"
                        + "  \"lookingFor\": \"WOMEN\",\n"
                        + "  \"searchDistance\": 100,\n"
                        + "  \"ageRangeMinimum\": 18,\n"
                        + "  \"ageRangeMaximum\": 100\n"
                        + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$").value(UPDATE_PROFILE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void updateProfileAndExpectUnprocessableEntityNotValid() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(
            put("/api/v1/profiles")
                .content(
                    "{\n"
                        + "  \"description\": \"string\",\n"
                        + "  \"mySong\":"
                        + " \"8CRUfdqO9fpirMnEMqNYbHsWsSS9UO7nySTaa1e2pf5cbuWgwH307TrCBwX07N\",\n"
                        + "  \"lastLatitude\": 1,\n"
                        + "  \"lastLongitude\": 1,\n"
                        + "  \"lookingFor\": \"WOMEN\",\n"
                        + "  \"searchDistance\": 100,\n"
                        + "  \"ageRangeMinimum\": 14,\n"
                        + "  \"ageRangeMaximum\": 100\n"
                        + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("The minimum age must be between 18 and 100"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void updateProfileAndExpectBadRequest() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(
            put("/api/v1/profiles")
                .content(
                    "{\n"
                        + "  \"desription\": \"string\",\n"
                        + "  \"mySog\":"
                        + " \"8CRUfdqO9fpirMnEMqNYbHsWsSS9UO7nySTaa1e2pf5cbuWgwH307TrCBwX07N\",\n"
                        + "  \"lastLtitude\": 1,\n"
                        + "  \"lastLngitude\": 1,\n"
                        + "  \"lookinFor\": \"WOMEN\",\n"
                        + "  \"searchDtance\": 100,\n"
                        + "  \"ageRainimum\": 18,\n"
                        + "  \"ageRangeMaximum\": 100\n"
                        + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(result -> result.getResponse().getContentAsString().contains("My song required"))
        .andExpect(
            result ->
                result.getResponse().getContentAsString().contains("The last latitude is required"))
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("The last longitude is required"))
        .andExpect(
            result ->
                result.getResponse().getContentAsString().contains("The looking for is required"))
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("The search distance is required"))
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("The age range minimum is required"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void updateLocationAndExpectOkAndNewValues() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(
            put("/api/v1/profiles/location")
                .content("{\n" + "  \"lastLatitude\": 0,\n" + "  \"lastLongitude\": 1\n" + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lastLatitude").value(0))
        .andExpect(jsonPath("$.lastLongitude").value(1));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void updateLocationAndExpectUnprocessableEntityNotValid() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(
            put("/api/v1/profiles/location")
                .content("{\n" + "  \"lastLatitude\": 181,\n" + "  \"lastLongitude\": 181\n" + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("The latitude must be between -90 and 90"))
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("The longitude must be between -180 and 180"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void updateLocationAndExpectBadRequest() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(
            put("/api/v1/profiles/location")
                .content("{\n" + "  \"lastLaiude\": 181,\n" + "  \"lasLonitude\": 181\n" + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                result.getResponse().getContentAsString().contains("The last latitude is required"))
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("The last longitude is required"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void updateLocationAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(
            put("/api/v1/profiles/location")
                .content("{\n" + "  \"lastLatitude\": 90,\n" + "  \"lastLongitude\": 90\n" + "}")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$").value(UPDATE_LOCATION_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void uploadPhotoAndExpectOkWithPhotoAddedAsLast() throws Exception {
    createProfileIfNotExists(profileId, profile);
    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart("/api/v1/profiles/photos");
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });
    mockMvc
        .perform(builder.file(photo).param("index", "-1").accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(greaterThan(0)))
        .andExpect(jsonPath("$.firstName").value(profile.getFirstName()))
        .andExpect(jsonPath("$.gender").value(profile.getGender().name()))
        .andExpect(jsonPath("$.photos", hasSize(profile.getPhotos().size() + 1)))
        .andExpect(jsonPath("$.description").value(profile.getDescription()))
        .andExpect(jsonPath("$.mySong").value(profile.getMySong()))
        .andExpect(jsonPath("$.lastLatitude").value(profile.getLastLatitude()))
        .andExpect(jsonPath("$.lastLongitude").value(profile.getLastLongitude()))
        .andExpect(jsonPath("$.swipesLeft").value(profile.getSwipesLeft()))
        .andExpect(jsonPath("$.waitUntil").value(profile.getWaitUntil()))
        .andExpect(jsonPath("$.lookingFor").value(profile.getLookingFor().name()))
        .andExpect(jsonPath("$.searchDistance").value(profile.getSearchDistance()))
        .andExpect(jsonPath("$.ageRangeMinimum").value(profile.getAgeRangeMinimum()))
        .andExpect(jsonPath("$.ageRangeMaximum").value(profile.getAgeRangeMaximum()))
        .andExpect(jsonPath("$._links.update.href").value("http://localhost/api/v1/profiles"))
        .andExpect(jsonPath("$._links.delete.href").value("http://localhost/api/v1/profiles"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void uploadPhotoAndExpectOkWithPhotoAddedAtCertainIndex() throws Exception {
    deleteProfileIfExistsById(profileId);
    createProfileIfNotExists(profileId, profile);
    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart("/api/v1/profiles/photos");
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });
    mockMvc
        .perform(builder.file(photo).param("index", "1").accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(greaterThan(0)))
        .andExpect(jsonPath("$.firstName").value(profile.getFirstName()))
        .andExpect(jsonPath("$.gender").value(profile.getGender().name()))
        .andExpect(jsonPath("$.photos", hasSize(profile.getPhotos().size())))
        .andExpect(jsonPath("$.description").value(profile.getDescription()))
        .andExpect(jsonPath("$.mySong").value(profile.getMySong()))
        .andExpect(jsonPath("$.lastLatitude").value(profile.getLastLatitude()))
        .andExpect(jsonPath("$.lastLongitude").value(profile.getLastLongitude()))
        .andExpect(jsonPath("$.swipesLeft").value(profile.getSwipesLeft()))
        .andExpect(jsonPath("$.waitUntil").value(profile.getWaitUntil()))
        .andExpect(jsonPath("$.lookingFor").value(profile.getLookingFor().name()))
        .andExpect(jsonPath("$.searchDistance").value(profile.getSearchDistance()))
        .andExpect(jsonPath("$.ageRangeMinimum").value(profile.getAgeRangeMinimum()))
        .andExpect(jsonPath("$.ageRangeMaximum").value(profile.getAgeRangeMaximum()))
        .andExpect(jsonPath("$._links.update.href").value("http://localhost/api/v1/profiles"))
        .andExpect(jsonPath("$._links.delete.href").value("http://localhost/api/v1/profiles"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void uploadPhotoAndExpectUnprocessableEntityPhotoLimitReached() throws Exception {
    deleteProfileIfExistsById(profileId);
    Profile modifiedProfile = profile;
    modifiedProfile.setPhotos(List.of("photo1", "photo2", "photo3", "photo4", "photo5"));
    createProfileIfNotExists(profileId, modifiedProfile);
    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart("/api/v1/profiles/photos");
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });
    mockMvc
        .perform(builder.file(photo).param("index", "-1").accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$").value(UPLOAD_PHOTO_UNPROCESSABLE_ENTITY_PHOTO_LIMIT_REACHED_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void uploadPhotoAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(profileId);
    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart("/api/v1/profiles/photos");
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });
    mockMvc
        .perform(builder.file(photo).param("index", "-1").accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$").value(UPLOAD_PHOTO_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void uploadPhotoAndExpectUnprocessableEntityNotValid() throws Exception {
    createProfileIfNotExists(profileId, profile);
    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart("/api/v1/profiles/photos");
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });
    mockMvc
        .perform(builder.file(photo).param("index", "5").accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("Photo number must be between -1 and 4"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void uploadPhotoAndExpectBadRequest() throws Exception {
    createProfileIfNotExists(profileId, profile);
    MockMultipartHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.multipart("/api/v1/profiles/photos");
    builder.with(
        new RequestPostProcessor() {
          @Override
          public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
            request.setMethod("PUT");
            return request;
          }
        });
    mockMvc
        .perform(builder.file(photo).param("i", "-1").accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void deletePhotoAndExpectOk() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(put("/api/v1/profiles/photos/{photoNumber}", 0))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(greaterThan(0)))
        .andExpect(jsonPath("$.firstName").value(profile.getFirstName()))
        .andExpect(jsonPath("$.gender").value(profile.getGender().name()))
        .andExpect(jsonPath("$.photos", hasSize(profile.getPhotos().size() - 1)))
        .andExpect(jsonPath("$.description").value(profile.getDescription()))
        .andExpect(jsonPath("$.mySong").value(profile.getMySong()))
        .andExpect(jsonPath("$.lastLatitude").value(profile.getLastLatitude()))
        .andExpect(jsonPath("$.lastLongitude").value(profile.getLastLongitude()))
        .andExpect(jsonPath("$.swipesLeft").value(profile.getSwipesLeft()))
        .andExpect(jsonPath("$.waitUntil").value(profile.getWaitUntil()))
        .andExpect(jsonPath("$.lookingFor").value(profile.getLookingFor().name()))
        .andExpect(jsonPath("$.searchDistance").value(profile.getSearchDistance()))
        .andExpect(jsonPath("$.ageRangeMinimum").value(profile.getAgeRangeMinimum()))
        .andExpect(jsonPath("$.ageRangeMaximum").value(profile.getAgeRangeMaximum()));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void deletePhotoAndExpectUnprocessableEntityNotValid() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(put("/api/v1/profiles/photos/{photoNumber}", 5))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            result ->
                result
                    .getResponse()
                    .getContentAsString()
                    .contains("Photo number must be between -1 and 4"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void deletePhotoAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(put("/api/v1/profiles/photos/{photoNumber}", 0))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$").value(DELETE_PHOTO_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void deletePhotoAndExpectBadRequest() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(put("/api/v1/profiles/photos/{photoNumber}", "a"))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void getSwipesLeftAndExpectOk() throws Exception {
    createProfileIfNotExists(profileId, profile);
    mockMvc
        .perform(get("/api/v1/profiles/swipesLeft"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", greaterThanOrEqualTo(0), lessThanOrEqualTo(50)).isNumber());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void getSwipesLeftAndExpectUnprocessableEntityProfileNotExists() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(get("/api/v1/profiles/swipesLeft"))
        .andDo(print())
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$").value(GET_SWIPES_LEFT_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void deleteProfileAndExpectNoContent() throws Exception {
    deleteProfileIfExistsById(profileId);
    mockMvc
        .perform(delete("/api/v1/profiles"))
        .andDo(print())
        .andExpect(status().isNoContent())
        .andExpect(jsonPath("$").doesNotExist());
  }
}
