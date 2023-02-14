package millimeeter.server.service;

import millimeeter.server.model.Match;
import millimeeter.server.model.Profile;
import millimeeter.server.repository.MatchRepository;
import millimeeter.server.repository.ProfileRepository;
import millimeeter.server.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthUtils {

  @Autowired private UserServiceImpl userServiceImpl;
  @Autowired private ProfileRepository profileRepository;
  @Autowired private MatchRepository matchRepository;

  public void saveProfile(Profile profile) {
    profileRepository.save(profile);
  }

  public boolean userExists() {
    return userServiceImpl.existsById(getAuthenticatedUserId());
  }

  // AUTHENTICATED USER ID
  public String getAuthenticatedUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication.getName();
  }

  // CORRESPONDING PROFILE ID
  public Long getProfileId() {
    Long profileId = userServiceImpl.getProfileId(getAuthenticatedUserId());
    if (profileId == null) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "Cannot get the profile id due to non-existing user");
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
    Match match = matchRepository.getReferenceById(matchId);
    return match.getProfileId1() == getProfileId() || match.getProfileId2() == getProfileId();
  }

  // MATCH EXISTS BY ID
  public boolean matchExistsById(Long matchId) {
    return matchRepository.findById(matchId).isPresent();
  }
}
