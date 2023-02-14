package millimeeter.server.service.impl;

import static millimeeter.server.controller.response.SwipeControllerResponses.*;

import java.time.LocalDateTime;
import millimeeter.server.enums.SwipeDirection;
import millimeeter.server.model.Match;
import millimeeter.server.model.Profile;
import millimeeter.server.model.Swipe;
import millimeeter.server.repository.SwipeRepository;
import millimeeter.server.service.AuthUtils;
import millimeeter.server.service.SwipeService;
import millimeeter.server.service.assembler.MatchModelAssembler;
import millimeeter.server.service.assembler.SwipeModelAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class SwipeServiceImpl implements SwipeService {

  @Autowired private SwipeRepository swipeRepository;
  @Autowired private MatchServiceImpl matchServiceImpl;
  @Autowired private AuthUtils authUtils;
  @Autowired private MatchModelAssembler matchModelAssembler;
  @Autowired private SwipeModelAssembler swipeModelAssembler;

  @Override
  public ResponseEntity<Object> swipe(Long id, String swipeDirection) {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      try {
        Profile profile = authUtils.getProfile();
        if (profile.getWaitUntil() != null && LocalDateTime.now().isAfter(profile.getWaitUntil())) {
          profile.setSwipesLeft(50);
        }
        if (profile.getSwipesLeft() == 0) {
          return new ResponseEntity<>(
              SWIPE_UNPROCESSABLE_ENTITY_NO_SWIPES_LEFT_RESPONSE, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (profile.getId() == id) {
          return new ResponseEntity<>(
              SWIPE_UNPROCESSABLE_ENTITY_CANNOT_SWIPE_YOURSELF_RESPONSE,
              HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (swipeRepository.countAllSwipesFromSenderToReceiver(profile.getId(), id) > 0) {
          return new ResponseEntity<>(SWIPE_CONFLICT_ALREADY_SWIPED_RESPONSE, HttpStatus.CONFLICT);
        }
        if (swipeRepository.countSelectedSwipesFromSenderToReceiver(
                    id, profile.getId(), SwipeDirection.RIGHT)
                > 0
            && swipeDirection.equals(SwipeDirection.RIGHT.name())) {
          Swipe swipe = new Swipe(profile.getId(), id, swipeDirection);
          swipeRepository.save(swipe);
          profile.setSwipesLeft(profile.getSwipesLeft() - 1);
          profile.setWaitUntil(LocalDateTime.now().plusHours(12));
          authUtils.saveProfile(profile);
          Match match = matchServiceImpl.addMatch(profile.getId(), id);
          return new ResponseEntity<>(matchModelAssembler.toModel(match), HttpStatus.CREATED);
        }
        Swipe swipe = new Swipe(profile.getId(), id, swipeDirection);
        swipe = swipeRepository.save(swipe);
        profile.setSwipesLeft(profile.getSwipesLeft() - 1);
        profile.setWaitUntil(LocalDateTime.now().plusHours(12));
        return new ResponseEntity<>(swipeModelAssembler.toModel(swipe), HttpStatus.CREATED);
      } catch (DataIntegrityViolationException e) {
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND, SWIPE_NOT_FOUND_SWIPED_PROFILE_NOT_EXISTS_YET_RESPONSE);
      }
    } else {
      return new ResponseEntity<>(
          SWIPE_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE, HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }
}
