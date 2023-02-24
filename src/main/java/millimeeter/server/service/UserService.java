package millimeeter.server.service;

import millimeeter.server.model.User;
import millimeeter.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User create() {
    String userId = getId();
    if (existsById(userId)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
    }
    try {
      return userRepository.createUser(userId);
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
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
