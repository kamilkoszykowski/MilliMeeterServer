package millimeeter.server.repository;

import java.util.List;
import javax.persistence.Tuple;
import millimeeter.server.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

  @Query(
      value =
          "SELECT matches.id AS match_id, profiles.id AS profile_id, profiles.first_name,"
              + " ARRAY_TO_STRING(profiles.photos, ';', '*') AS photos, matches.matched_at FROM"
              + " matches JOIN profiles ON (profiles.id = matches.profile_id_1 OR profiles.id ="
              + " matches.profile_id_2) WHERE matches.id NOT IN (SELECT match_id FROM"
              + " public.messages) AND profiles.id != ?1 AND (matches.profile_id_1 = ?1 OR"
              + " matches.profile_id_2 = ?1) ORDER BY matches.matched_at DESC;",
      nativeQuery = true)
  List<Tuple> findAllMatchesWithoutMessages(Long profileId);

  @Query(
      value =
          "SELECT * FROM (SELECT DISTINCT ON (matches.id) matches.id AS match_id, profiles.id AS"
              + " profile_id, profiles.first_name, ARRAY_TO_STRING(profiles.photos, ';', '*') AS"
              + " photos, messages.sender_id, messages.content, messages.status, messages.sent_at"
              + " FROM matches JOIN messages ON matches.id = messages.match_id JOIN profiles ON"
              + " (matches.profile_id_1 = profiles.id OR matches.profile_id_2 = profiles.id) WHERE"
              + " profiles.id != ?1 AND (matches.profile_id_1 = ?1 OR matches.profile_id_2 = ?1)"
              + " ORDER BY match_id, messages.sent_at DESC) m ORDER BY m.sent_at DESC;",
      nativeQuery = true)
  List<Tuple> findAllMatchesWithMessages(Long profileId);
}
