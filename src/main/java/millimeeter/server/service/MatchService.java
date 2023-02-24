package millimeeter.server.service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.Tuple;
import millimeeter.server.dto.MatchDto;
import millimeeter.server.dto.MatchWithMessagesDto;
import millimeeter.server.enums.MessageStatus;
import millimeeter.server.model.Match;
import millimeeter.server.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MatchService {

  private final MatchRepository matchRepository;
  private final AuthUtils authUtils;

  @Autowired
  public MatchService(MatchRepository matchRepository, AuthUtils authUtils) {
    this.matchRepository = matchRepository;
    this.authUtils = authUtils;
  }

  public List<MatchDto> findAllMatches() {
    authUtils.checkIfProfileExists();
    return projectTupleToMatchDtoList(
        matchRepository.findAllMatchesWithoutMessages(authUtils.getProfileId()));
  }

  public List<MatchWithMessagesDto> findAllMatchesWithMessages() {
    authUtils.checkIfProfileExists();
    return projectTupleToMatchWithMessagesDtoList(
        matchRepository.findAllMatchesWithMessages(authUtils.getProfileId()));
  }

  public void deleteMatchById(Long id) {
    authUtils.checkIfProfileExists();
    if (!authUtils.matchExistsById(id)) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Match not exists");
    }
    Match match = matchRepository.getReferenceById(id);
    Long profileId = authUtils.getProfileId();
    if (Objects.equals(match.getProfileId1(), profileId)
        || Objects.equals(match.getProfileId2(), profileId)) {
      matchRepository.deleteById(id);
    }
  }

  public Match addMatch(Long profileId1, Long profileId2) {
    try {
      Match m = new Match(profileId1, profileId2);
      return matchRepository.save(m);
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Swiped profile not exists");
    }
  }

  List<MatchDto> projectTupleToMatchDtoList(List<Tuple> tupleList) {
    return tupleList.stream()
        .map(
            t ->
                new MatchDto(
                    t.get("match_id", BigInteger.class).longValue(),
                    t.get("profile_id", BigInteger.class).longValue(),
                    t.get("first_name", String.class),
                    t.get("photos", String.class).split(";"),
                    t.get("matched_at", Timestamp.class).toLocalDateTime()))
        .collect(Collectors.toList());
  }

  List<MatchWithMessagesDto> projectTupleToMatchWithMessagesDtoList(List<Tuple> tupleList) {
    return tupleList.stream()
        .map(
            t ->
                new MatchWithMessagesDto(
                    t.get("match_id", BigInteger.class).longValue(),
                    t.get("profile_id", BigInteger.class).longValue(),
                    t.get("first_name", String.class),
                    t.get("photos", String.class).split(";"),
                    t.get("sender_id", BigInteger.class).longValue(),
                    t.get("content", String.class),
                    MessageStatus.valueOf(t.get("status", String.class)),
                    t.get("sent_at", Timestamp.class).toLocalDateTime()))
        .collect(Collectors.toList());
  }
}
