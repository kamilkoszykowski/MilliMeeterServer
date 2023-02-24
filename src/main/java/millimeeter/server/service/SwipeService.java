package millimeeter.server.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import millimeeter.server.enums.SwipeDirection;
import millimeeter.server.model.Match;
import millimeeter.server.model.Profile;
import millimeeter.server.model.Swipe;
import millimeeter.server.repository.SwipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class SwipeService {

  private final SwipeRepository swipeRepository;
  private final MatchService matchServiceImpl;
  private final AuthUtils authUtils;

  @Autowired
  public SwipeService(
      SwipeRepository swipeRepository, MatchService matchServiceImpl, AuthUtils authUtils) {
    this.swipeRepository = swipeRepository;
    this.matchServiceImpl = matchServiceImpl;
    this.authUtils = authUtils;
  }

  public Map<String, Object> swipe(Long id, String swipeDirection) {
    authUtils.checkIfProfileExists();
    try {
      Profile profile = authUtils.getProfile();
      if (profile.getWaitUntil() != null && LocalDateTime.now().isAfter(profile.getWaitUntil())) {
        profile.setSwipesLeft(50);
      }
      if (profile.getSwipesLeft() == 0) {
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No swipes left");
      }
      if (Objects.equals(profile.getId(), id)) {
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "You swiped yourself");
      }
      if (swipeRepository.countAllSwipesFromSenderToReceiver(profile.getId(), id) > 0) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "You already swiped that profile");
      }
      Swipe swipe = new Swipe(profile.getId(), id, swipeDirection);
      swipe = swipeRepository.save(swipe);
      profile.setSwipesLeft(profile.getSwipesLeft() - 1);
      profile.setWaitUntil(LocalDateTime.now().plusHours(12));
      if (swipeRepository.countSelectedSwipesFromSenderToReceiver(
                  id, profile.getId(), SwipeDirection.RIGHT)
              > 0
          && swipeDirection.equals(SwipeDirection.RIGHT.name())) {
        Match match = matchServiceImpl.addMatch(profile.getId(), id);
        authUtils.saveProfile(profile);
        return new HashMap<>(Map.of("match", match));
      } else {
        authUtils.saveProfile(profile);
        return new HashMap<>(Map.of("swipe", swipe));
      }
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Swiped profile not exists");
    }
  }
}
