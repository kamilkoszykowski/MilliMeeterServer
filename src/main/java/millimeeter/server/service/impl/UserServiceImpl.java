package millimeeter.server.service.impl;

import static millimeeter.server.controller.response.UserControllerResponses.CREATE_USER_CONFLICT_RESPONSE;

import millimeeter.server.model.User;
import millimeeter.server.repository.UserRepository;
import millimeeter.server.service.UserService;
import millimeeter.server.service.assembler.UserModelAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

  @Autowired private UserRepository userRepository;
  @Autowired private UserModelAssembler userModelAssembler;

  @Override
  public ResponseEntity<Object> create() {
    try {
      String userId = getId();
      if (existsById(userId)) {
        return new ResponseEntity<>(CREATE_USER_CONFLICT_RESPONSE, HttpStatus.CONFLICT);
      } else {
        User user = userRepository.createUser(userId);
        return new ResponseEntity<>(userModelAssembler.toModel(user), HttpStatus.CREATED);
      }
    } catch (DataIntegrityViolationException e) {
      return new ResponseEntity<>(CREATE_USER_CONFLICT_RESPONSE, HttpStatus.CONFLICT);
    }
  }

  public boolean existsById(String id) {
    return userRepository.findById(id).isPresent();
  }

  public String getId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication.getName();
  }

  public Long getProfileId(String userId) {
    return userRepository.findProfileIdById(userId);
  }
}
