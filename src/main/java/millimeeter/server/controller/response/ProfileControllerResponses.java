package millimeeter.server.controller.response;

public class ProfileControllerResponses {

  // ENDPOINT: "/profiles"
  public static final String CREATE_CONFLICT_RESPONSE =
      "Cannot create the profile because it already exists";
  public static final String CREATE_CREATED_RESPONSE =
      "{\"id\":53,\"firstName\":\"Phhjdnjuahunyksfu\",\"dateOfBirth\":\"2000-01-31\",\"gender\":\"WOMAN\",\"photos\":[\"16760234755263350ef79-c6af-4665-a6c0-ac9b92355dfd.jpg\",\"16760234755273a0e4764-059b-4760-a89d-2b9880da57f9.jpg\",\"1676023475527153e0068-6095-4195-ad0f-1967d3db9a37.jpg\",\"167602347552830572e96-ba09-4e05-b298-0270656aa7d8.jpg\",\"16760234755285391781f-4f02-4a52-9398-70e6e473990c.jpg\"],\"description\":\"string\",\"mySong\":\"9QJm7ECVsC0Lygh33KbiwdqCvl5txHfVJERBtMYO6kEvOfRA71slgcb6Kc3Fu7KQxFX9LD4KO\",\"lastLatitude\":90.0,\"lastLongitude\":90.0,\"createdAt\":\"2023-02-10T11:04:35.5305578\",\"swipesLeft\":50,\"waitUntil\":null,\"lookingFor\":\"WOMEN\",\"searchDistance\":100,\"ageRangeMinimum\":100,\"ageRangeMaximum\":100,\"_links\":{\"update\":{\"href\":\"http://localhost/api/v1/profiles\"},\"delete\":{\"href\":\"http://localhost/api/v1/profiles\"}}}";
  public static final String CREATE_UNPROCESSABLE_ENTITY_TOO_MANY_PHOTOS_RESPONSE =
      "Cannot upload more than 5 photos";

  // ENDPOINT: "/profiles/photos/{index}"
  public static final String DELETE_PHOTO_BAD_REQUEST_RESPONSE = "";
  public static final String DELETE_PHOTO_OK_RESPONSE =
      "{\"id\":53,\"firstName\":\"ProfileOne\",\"dateOfBirth\":\"2000-01-01\",\"gender\":\"MAN\",\"photos\":[\"photo2.jpg\"],\"description\":\"description1\",\"mySong\":\"mySong\",\"lastLatitude\":90.0,\"lastLongitude\":90.0,\"createdAt\":\"2023-02-10T12:27:36.427556\",\"swipesLeft\":50,\"waitUntil\":null,\"lookingFor\":\"WOMEN\",\"searchDistance\":100,\"ageRangeMinimum\":18,\"ageRangeMaximum\":40}";
  public static final String DELETE_PHOTO_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE =
      "{\"errors\":[\"Photo number must be between -1 and 4\"]}";
  public static final String DELETE_PHOTO_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
      "Cannot delete the photo due to non-existing profile";

  // ENDPOINT: "/profiles"
  public static final String FIND_PROFILES_TO_SWIPE_OK_NO_PROFILES_RESPONSE = "{}";
  public static final String FIND_PROFILES_TO_SWIPE_OK_WITH_PROFILES =
      "{\"_embedded\":{\"profileToSwipeDtoList\":[{\"id\":42,\"firstName\":\"ProfileTwo\",\"age\":23,\"gender\":\"WOMAN\",\"photos\":[\"anotherPhoto1.jpg\",\"anotherPhoto2.jpg\"],\"description\":\"another"
          + " description\",\"mySong\":\"another mySong\",\"distanceAway\":69,\"_links\":{\"swipe"
          + " left\":{\"href\":\"http://localhost/api/v1/swipes/42/LEFT\"},\"swipe"
          + " right\":{\"href\":\"http://localhost/api/v1/swipes/42/RIGHT\"}}},{\"id\":8,\"firstName\":\"Chicktwo\",\"age\":23,\"gender\":\"WOMAN\",\"photos\":[\"16751103159659e6bf505-18ac-4f81-babb-4afb509ee14c.jpg\",\"16751103159687496ac15-01fb-4471-95b3-aaf791bb87f0.jpg\"],\"description\":\"Want"
          + " to join to my private snapchat?"
          + " :3\",\"mySong\":\"jfjhjzdfhiuhrhuiguihrg\",\"distanceAway\":69,\"_links\":{\"swipe"
          + " left\":{\"href\":\"http://localhost/api/v1/swipes/8/LEFT\"},\"swipe"
          + " right\":{\"href\":\"http://localhost/api/v1/swipes/8/RIGHT\"}}}]}}";
  public static final String
      FIND_PROFILES_TO_SWIPE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
          "Cannot find profiles to swipe due to non-existing profile";

  // ENDPOINT: "/profiles/me"
  public static final String GET_MY_PROFILE_OK_RESPONSE =
      "{\"firstName\":\"ProfileOne\",\"dateOfBirth\":\"2000-01-01\",\"gender\":\"MAN\",\"photos\":[\"photo1.jpg\",\"photo2.jpg\"],\"description\":\"description1\",\"mySong\":\"mySong\",\"lookingFor\":\"WOMEN\",\"searchDistance\":100,\"ageRangeMinimum\":18,\"ageRangeMaximum\":40,\"_links\":{\"update\":{\"href\":\"http://localhost/api/v1/profiles\"},\"delete\":{\"href\":\"http://localhost/api/v1/profiles\"}}}";
  public static final String GET_MY_PROFILE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
      "Cannot get the profile because it does not exist";

  // ENDPOINT: "/profiles/swipesLeft"
  public static final String GET_SWIPES_LEFT_OK_RESPONSE = "50";
  public static final String GET_SWIPES_LEFT_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
      "Cannot get swipes left count due to non-existing profile";

  // ENDPOINT: "/profiles/location"
  public static final String UPDATE_LOCATION_BAD_REQUEST_RESPONSE =
      "{\"errors\":[\"The last latitude is required\",\"The last longitude is required\"]}";
  public static final String UPDATE_LOCATION_OK_AND_NEW_VALUES_RESPONSE =
      "{\"id\":53,\"firstName\":\"ProfileOne\",\"dateOfBirth\":\"2000-01-01\",\"gender\":\"MAN\",\"photos\":[\"photo1.jpg\",\"photo2.jpg\"],\"description\":\"description1\",\"mySong\":\"mySong\",\"lastLatitude\":0.0,\"lastLongitude\":1.0,\"createdAt\":\"2023-02-10T12:27:36.776866\",\"swipesLeft\":50,\"waitUntil\":null,\"lookingFor\":\"WOMEN\",\"searchDistance\":100,\"ageRangeMinimum\":18,\"ageRangeMaximum\":40,\"_links\":{\"update\":{\"href\":\"http://localhost/api/v1/profiles\"},\"delete\":{\"href\":\"http://localhost/api/v1/profiles\"}}}";
  public static final String UPDATE_LOCATION_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE =
      "{\"errors\":[\"The latitude must be between -90 and 90\",\"The longitude must be between"
          + " -180 and 180\"]}";
  public static final String UPDATE_LOCATION_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
      "Cannot update the location due to non-existing profile";

  // ENDPOINT: "/profiles"
  public static final String UPDATE_PROFILE_BAD_REQUEST_RESPONSE =
      "{\"errors\":[\"The age range minimum is required\",\"My song required\",\"The last latitude"
          + " is required\",\"The search distance is required\",\"The looking for is"
          + " required\",\"The last longitude is required\"]}";
  public static final String UPDATE_PROFILE_OK_RESPONSE =
      "{\"id\":53,\"firstName\":\"ProfileOne\",\"dateOfBirth\":\"2000-01-01\",\"gender\":\"MAN\",\"photos\":[\"photo1.jpg\",\"photo2.jpg\"],\"description\":\"string\",\"mySong\":\"8CRUfdqO9fpirMnEMqNYbHsWsSS9UO7nySTaa1e2pf5cbuWgwH307TrCBwX07N\",\"lastLatitude\":1.0,\"lastLongitude\":1.0,\"createdAt\":\"2023-02-10T12:27:36.116256\",\"swipesLeft\":50,\"waitUntil\":null,\"lookingFor\":\"WOMEN\",\"searchDistance\":100,\"ageRangeMinimum\":18,\"ageRangeMaximum\":100,\"_links\":{\"update\":{\"href\":\"http://localhost/api/v1/profiles\"},\"delete\":{\"href\":\"http://localhost/api/v1/profiles\"}}}";
  public static final String UPDATE_PROFILE_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE =
      "{\"errors\":[\"The minimum age must be between 18 and 100\"]}";
  public static final String UPDATE_PROFILE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
      "Cannot update the profile because it does not exist";

  // ENDPOINT: "/profiles/photos"
  public static final String UPLOAD_PHOTO_BAD_REQUEST_RESPONSE = "";
  public static final String UPLOAD_PHOTO_OK_WITH_PHOTO_ADDED_AS_LAST_RESPONSE =
      "{\"id\":53,\"firstName\":\"ProfileOne\",\"dateOfBirth\":\"2000-01-01\",\"gender\":\"MAN\",\"photos\":[\"photo1.jpg\",\"photo2.jpg\",\"1676028456549b5b80dc6-affd-441a-8be2-fb4f1cfa3393.jpg\"],\"description\":\"description1\",\"mySong\":\"mySong\",\"lastLatitude\":90.0,\"lastLongitude\":90.0,\"createdAt\":\"2023-02-10T12:27:36.532644\",\"swipesLeft\":50,\"waitUntil\":null,\"lookingFor\":\"WOMEN\",\"searchDistance\":100,\"ageRangeMinimum\":18,\"ageRangeMaximum\":40,\"_links\":{\"update\":{\"href\":\"http://localhost/api/v1/profiles\"},\"delete\":{\"href\":\"http://localhost/api/v1/profiles\"}}}";
  public static final String UPLOAD_PHOTO_OK_WITH_PHOTO_ADDED_AT_CERTAIN_INDEX_RESPONSE =
      "{\"id\":53,\"firstName\":\"ProfileOne\",\"dateOfBirth\":\"2000-01-01\",\"gender\":\"MAN\",\"photos\":[\"photo1.jpg\",\"167602845658100c02617-5080-4afd-91c2-6db2450d7306.jpg\"],\"description\":\"description1\",\"mySong\":\"mySong\",\"lastLatitude\":90.0,\"lastLongitude\":90.0,\"createdAt\":\"2023-02-10T12:27:36.560661\",\"swipesLeft\":50,\"waitUntil\":null,\"lookingFor\":\"WOMEN\",\"searchDistance\":100,\"ageRangeMinimum\":18,\"ageRangeMaximum\":40,\"_links\":{\"update\":{\"href\":\"http://localhost/api/v1/profiles\"},\"delete\":{\"href\":\"http://localhost/api/v1/profiles\"}}}";
  public static final String UPLOAD_PHOTO_UNPROCESSABLE_ENTITY_NOT_VALID_RESPONSE =
      "{\"errors\":[\"Photo number must be between -1 and 4\"]}";
  public static final String UPLOAD_PHOTO_UNPROCESSABLE_ENTITY_PHOTO_LIMIT_REACHED_RESPONSE =
      "Cannot upload photo due to 5 photos per profile limit";
  public static final String UPLOAD_PHOTO_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE =
      "Cannot upload the photo due to non-existing profile";

  // ENDPOINT: "/profiles"
  public static final String DELETE_PROFILE_NO_CONTENT_RESPONSE = "";
}
