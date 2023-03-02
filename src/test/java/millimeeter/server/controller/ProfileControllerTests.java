package millimeeter.server.controller;

import millimeeter.server.dto.RegistrationDto;
import millimeeter.server.model.Profile;
import millimeeter.server.service.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTests {

  private final TestUtils testUtils;
  private final WebApplicationContext applicationContext;
  private MockMvc mockMvc;

  @Autowired
  public ProfileControllerTests(
      TestUtils testUtils,
      WebApplicationContext applicationContext) {
    this.testUtils = testUtils;
    this.applicationContext = applicationContext;
  }

  static final String USERNAME = TestUtils.USERNAME;
  private Profile profile;
  private Profile anotherProfile;
  private MockMultipartFile photo;
  private MockMultipartFile invalidPhoto;
  private MockMultipartFile body;


  @BeforeEach
  void init() {
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(applicationContext)
            .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
            .build();
    testUtils.createUserIfNotExists(TestUtils.USERNAME, testUtils.getProfile());
    testUtils.createUserIfNotExists(TestUtils.ANOTHER_USERNAME, testUtils.getAnotherProfile());
    testUtils.createProfileIfNotExists(testUtils.getProfile());
    profile = testUtils.getProfile();
    anotherProfile = testUtils.getAnotherProfile();
    photo = testUtils.getPhoto();
    invalidPhoto = testUtils.getInvalidPhoto();
    body = testUtils.getBody();
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyFindingProfilesToSwipeReturnOkNoProfiles() throws Exception {
    testUtils.deleteProfileIfExistsById(anotherProfile);
    mockMvc
        .perform(get("/api/v1/profiles"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyFindingProfilesToSwipeReturnOkWithProfiles() throws Exception {
    testUtils.deleteProfileIfExistsById(anotherProfile);
    testUtils.createProfileIfNotExists(anotherProfile);
    mockMvc
        .perform(get("/api/v1/profiles"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.profileToSwipeDtoList.size()", greaterThan(0)))
        .andExpect(
            jsonPath("$._embedded.profileToSwipeDtoList[?(@.id == " + anotherProfile.getId() + ")]", hasSize(1)))
        .andExpect(
            jsonPath("$._embedded.profileToSwipeDtoList[?(@.id == " + anotherProfile.getId() + ")].firstName")
                .value(anotherProfile.getFirstName()))
        .andExpect(
            jsonPath("$._embedded.profileToSwipeDtoList[?(@.id == " + anotherProfile.getId() + ")].age")
                .value(
                    LocalDateTime.now()
                        .minusYears(anotherProfile.getDateOfBirth().getYear())
                        .getYear()))
        .andExpect(
            jsonPath("$._embedded.profileToSwipeDtoList[?(@.id == " + anotherProfile.getId() + ")].gender")
                .value(anotherProfile.getGender().name()))
        .andExpect(
            jsonPath("$._embedded.profileToSwipeDtoList[?(@.id == " + anotherProfile.getId() + ")].description")
                .value(anotherProfile.getDescription()))
        .andExpect(
            jsonPath("$._embedded.profileToSwipeDtoList[?(@.id == " + anotherProfile.getId() + ")].mySong")
                .value(anotherProfile.getMySong()))
        .andExpect(
            jsonPath(
                    "$._embedded.profileToSwipeDtoList[?(@.id == "
                        + anotherProfile.getId()
                        + " && @.distanceAway <= 100)]")
                .exists())
        .andExpect(
            jsonPath(
                    "$._embedded.profileToSwipeDtoList[?(@.id == "
                        + anotherProfile.getId()
                        + " && @.photos.size() == 2)]")
                .exists())
        .andExpect(
            jsonPath(
                    "$._embedded.profileToSwipeDtoList[?(@.id == "
                        + anotherProfile.getId()
                        + ")]._links.['swipe left'].href")
                .value("http://localhost/api/v1/swipes/" + anotherProfile.getId() + "/LEFT"))
        .andExpect(
            jsonPath(
                    "$._embedded.profileToSwipeDtoList[?(@.id == "
                        + anotherProfile.getId()
                        + ")]._links.['swipe right'].href")
                .value("http://localhost/api/v1/swipes/" + anotherProfile.getId() + "/RIGHT"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyGettingMyProfileReturnOk() throws Exception {
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
  void verifyCreatingProfileReturnCreated() throws Exception {
    testUtils.deleteProfileIfExistsById(profile);
    RegistrationDto registrationDto = testUtils.getRegistrationDto();
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
            .andDo(result -> {
              testUtils.deleteProfileIfExistsById(profile);
            })
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
  void verifyCreatingAlreadyExistingProfileReturnConflict() throws Exception {
    mockMvc
        .perform(
            multipart("/api/v1/profiles").file(photo).file(body).accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors").value("Profile already exists"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyCreatingProfileWithTooManyPhotosReturnUnprocessableEntity() throws Exception {
    testUtils.deleteProfileIfExistsById(profile);
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
        .andExpect(jsonPath("$.errors").value("Limit of 5 photos exceeded"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyCreatingProfileWithInvalidPhotosReturnUnprocessableEntity() throws Exception {
    testUtils.deleteProfileIfExistsById(profile);
    mockMvc
            .perform(
                    multipart("/api/v1/profiles")
                            .file(invalidPhoto)
                            .file(invalidPhoto)
                            .file(photo)
                            .file(photo)
                            .file(photo)
                            .file(body)
                            .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andDo(result -> {
              testUtils.deleteProfileIfExistsById(profile);
            })
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors").value("Photos must have 3/4 aspect ratio and .jpg extension"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyUpdatingProfileReturnOk() throws Exception {
    testUtils.deleteProfileIfExistsById(profile);
    testUtils.createProfileIfNotExists(profile);
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
  void verifyUpdatingLocationReturnOkAndNewValues() throws Exception {
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
  void verifyUploadingPhotoReturnOkWithPhotoAddedAsLast() throws Exception {
    testUtils.deleteProfileIfExistsById(profile);
    Profile modifiedProfile = profile;
    modifiedProfile.setPhotos(List.of("photo1.jpg", "photo2.jpg"));
    testUtils.createProfileIfNotExists(modifiedProfile);
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
  void verifyUploadingPhotoReturnOkWithPhotoAddedAtCertainIndex() throws Exception {
    testUtils.deleteProfileIfExistsById(profile);
    testUtils.createProfileIfNotExists(profile);
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
  void verifyUploadingInvalidPhotoReturnUnprocessableEntity() throws Exception {
    testUtils.deleteProfileIfExistsById(profile);
    testUtils.createProfileIfNotExists(profile);
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
            .perform(builder.file(invalidPhoto).param("index", "1").accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors").value("Photos must have 3/4 aspect ratio and .jpg extension"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyUploadingPhotoWithLimitReachedReturnUnprocessableEntity() throws Exception {
    testUtils.deleteProfileIfExistsById(profile);
    Profile unmodifiedProfile = testUtils.getProfile();
    Profile modifiedProfile = new Profile(unmodifiedProfile);
    modifiedProfile.setPhotos(
        List.of("photo1.jpg", "photo2.jpg", "photo3.jpg", "photo4.jpg", "photo5.jpg"));
    testUtils.createProfileIfNotExists(modifiedProfile);
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
            .andDo(result -> {
              testUtils.deleteProfileIfExistsById(profile);
              testUtils.createProfileIfNotExists(unmodifiedProfile);
            })
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors").value("Limit of 5 photos reached"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyDeletingPhotoReturnOk() throws Exception {
    testUtils.deleteProfileIfExistsById(profile);
    testUtils.createProfileIfNotExists(profile);
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
  void verifyDeletingLastRemainingPhotoReturnUnprocessableEntity() throws Exception {
    testUtils.deleteProfileIfExistsById(profile);
    Profile unmodifiedProfile = testUtils.getProfile();
    Profile modifiedProfile = new Profile(unmodifiedProfile);
    modifiedProfile.setPhotos(List.of("photo1.jpg"));
    testUtils.createProfileIfNotExists(modifiedProfile);
    mockMvc
            .perform(put("/api/v1/profiles/photos/{photoNumber}", 0))
            .andDo(print())
            .andDo(result -> {
              testUtils.deleteProfileIfExistsById(profile);
              testUtils.createProfileIfNotExists(unmodifiedProfile);
            })
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors").value("Cannot delete the only remaining photo"));
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyGettingSwipesLeftReturnOk() throws Exception {
    mockMvc
        .perform(get("/api/v1/profiles/swipesLeft"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", greaterThanOrEqualTo(0), lessThanOrEqualTo(50)).isNumber());
  }

  @Test
  @WithMockUser(username = USERNAME)
  void verifyDeletingProfileReturnNoContent() throws Exception {
    testUtils.deleteProfileIfExistsById(profile);
    mockMvc
        .perform(delete("/api/v1/profiles"))
        .andDo(print())
        .andExpect(status().isNoContent())
        .andExpect(jsonPath("$").doesNotExist());
  }
}
