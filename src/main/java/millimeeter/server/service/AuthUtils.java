package millimeeter.server.service;

import millimeeter.server.model.Match;
import millimeeter.server.model.Profile;
import millimeeter.server.repository.MatchRepository;
import millimeeter.server.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthUtils {

  private final UserService userService;
  private final ProfileRepository profileRepository;
  private final MatchRepository matchRepository;

  @Autowired
  public AuthUtils(
      UserService userService,
      ProfileRepository profileRepository,
      MatchRepository matchRepository) {
    this.userService = userService;
    this.profileRepository = profileRepository;
    this.matchRepository = matchRepository;
  }

  public void saveProfile(Profile profile) {
    profileRepository.save(profile);
  }

  public boolean userExists() {
    return userService.existsById(getAuthenticatedUserId());
  }

  // AUTHENTICATED USER ID
  public String getAuthenticatedUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication.getName();
  }

  // CORRESPONDING PROFILE ID
  public Long getProfileId() {
    Long profileId = userService.getProfileId(getAuthenticatedUserId());
    if (profileId == null) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Profile not exists");
    }
    return profileId;
  }

  // CORRESPONDING PROFILE
  public Profile getProfile() {
    return profileRepository.findById(getProfileId()).get();
  }

  // PROFILE EXISTS BY ID
  public boolean profileExistsById(Long id) {
    return profileRepository.findById(id).isPresent();
  }

  // PROFILE BELONGS TO MATCH
  public boolean profileBelongsToMatch(Long matchId) {
    Match match = matchRepository.findById(matchId).get();
    long profileId = getProfileId();
    return (match.getProfileId1() == profileId) || (match.getProfileId2() == profileId);
  }

  // MATCH EXISTS BY ID
  public boolean matchExistsById(Long matchId) {
    return matchRepository.findById(matchId).isPresent();
  }

  public void checkIfUserExists() {
    if (!userExists()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "User not exists");
    }
  }

  public void checkIfProfileExists() {
    checkIfUserExists();
    if (!profileExistsById(getProfileId())) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Profile not exists");
    }
  }

  public void checkIfMatchExists(Long matchId) {
    if (!matchExistsById(matchId)) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Match not exists");
    }
  }

  public void checkIfProfileBelongsToMatch(Long matchId) {
    if (!profileBelongsToMatch(matchId)) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "Profile not belongs to match");
    }
  }

  public void checkIfProfileBelongsToMatchContainingMessage(Long matchId) {
    if (!profileBelongsToMatch(matchId)) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "Profile not belongs to match containing given message");
    }
  }
}
