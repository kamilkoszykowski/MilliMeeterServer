package millimeeter.server.service;

import org.springframework.http.ResponseEntity;

public interface SwipeService {
  ResponseEntity<Object> swipe(Long id, String swipeDirection);
}
