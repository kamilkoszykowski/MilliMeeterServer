package millimeeter.server.service.impl;

import static millimeeter.server.controller.response.MatchControllerResponses.*;
import static millimeeter.server.controller.response.SwipeControllerResponses.*;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Tuple;
import millimeeter.server.dto.MatchDto;
import millimeeter.server.dto.MatchWithMessagesDto;
import millimeeter.server.enums.MessageStatus;
import millimeeter.server.model.Match;
import millimeeter.server.repository.MatchRepository;
import millimeeter.server.service.AuthUtils;
import millimeeter.server.service.MatchService;
import millimeeter.server.service.assembler.MatchDtoModelAssembler;
import millimeeter.server.service.assembler.MatchModelAssembler;
import millimeeter.server.service.assembler.MatchWithMessagesDtoModelAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MatchServiceImpl implements MatchService {

  @Autowired private MatchRepository matchRepository;
  @Autowired private AuthUtils authUtils;
  @Autowired private MatchModelAssembler matchModelAssembler;
  @Autowired private MatchDtoModelAssembler matchDtoModelAssembler;
  @Autowired private MatchWithMessagesDtoModelAssembler matchWithMessagesDtoModelAssembler;

  @Override
  public ResponseEntity<Object> findAllMatches() {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      List<MatchDto> matches =
          projectTupleToMatchDtoList(
              matchRepository.findAllMatchesWithoutMessages(authUtils.getProfileId()));
      List<EntityModel<MatchDto>> matchesModel =
          matches.stream().map(matchDtoModelAssembler::toModel).collect(Collectors.toList());
      return new ResponseEntity<>(CollectionModel.of(matchesModel), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(
          FIND_MATCHES_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE,
          HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @Override
  public ResponseEntity<Object> findAllMatchesWithMessages() {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      List<MatchWithMessagesDto> matches =
          projectTupleToMatchWithMessagesDtoList(
              matchRepository.findAllMatchesWithMessages(authUtils.getProfileId()));
      List<EntityModel<MatchWithMessagesDto>> matchesModel =
          matches.stream()
              .map(matchWithMessagesDtoModelAssembler::toModel)
              .collect(Collectors.toList());
      return new ResponseEntity<>(CollectionModel.of(matchesModel), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(
          FIND_MATCHES_WITH_MESSAGES_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE,
          HttpStatus.UNPROCESSABLE_ENTITY);
    }
  }

  @Override
  public ResponseEntity<Void> deleteMatchById(Long id) {
    if (authUtils.profileExistsById(authUtils.getProfileId())) {
      if (matchRepository.findById(id).isPresent()) {
        Match match = matchRepository.getReferenceById(id);
        Long profileId = authUtils.getProfileId();
        if (match.getProfileId1() == profileId || match.getProfileId2() == profileId) {
          matchRepository.deleteById(id);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      } else {
        throw new ResponseStatusException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            DELETE_MATCH_UNPROCESSABLE_ENTITY_MATCH_NOT_EXISTS_RESPONSE);
      }
    } else {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          DELETE_MATCH_UNPROCESSABLE_ENTITY_PROFILE_NOT_EXISTS_RESPONSE);
    }
  }

  public Match addMatch(Long profileId1, Long profileId2) {
    try {
      Match m = new Match(profileId1, profileId2);
      return matchRepository.save(m);
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, SWIPE_NOT_FOUND_SWIPED_PROFILE_NOT_EXISTS_YET_RESPONSE);
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
