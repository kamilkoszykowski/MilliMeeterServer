package millimeeter.server.service;

import org.springframework.http.ResponseEntity;

public interface MatchService {
  ResponseEntity<Object> findAllMatches();

  ResponseEntity<Object> findAllMatchesWithMessages();

  ResponseEntity<Void> deleteMatchById(Long id);
}
