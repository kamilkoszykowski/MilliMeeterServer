package millimeeter.server.repository;

import java.time.LocalDate;
import java.util.List;
import javax.persistence.Tuple;
import millimeeter.server.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

  @Query(value = "SELECT p.swipesLeft FROM profiles p WHERE p.id = ?1")
  int getSwipesLeftById(Long id);

  @Query(
      value =
          "SELECT id, first_name, EXTRACT(YEAR FROM AGE(CURRENT_DATE, date_of_birth)) AS age,"
              + " gender, ARRAY_TO_STRING(photos, ';', '*') AS photos, description, my_song,"
              + " point(?5, ?6)<@>point(last_longitude, last_latitude) AS distance FROM profiles"
              + " WHERE id != ?2 AND gender = CAST(?1 AS gender) AND NOT EXISTS (SELECT 1 FROM"
              + " swipes WHERE receiver_id = profiles.id AND sender_id = ?2) AND date_of_birth"
              + " BETWEEN ?3 AND ?4 AND point(?5, ?6)<@>point(last_longitude, last_latitude) < ?7"
              + " ORDER BY random() LIMIT 50;",
      nativeQuery = true)
  List<Tuple> findProfilesByGenderToSwipe(
      String gender,
      Long userId,
      LocalDate dateOfBirthRangeStart,
      LocalDate dateOfBirthRangeEnd,
      Double longitude,
      Double latitude,
      Integer searchDistance);

  @Query(
      value =
          "SELECT id, first_name, EXTRACT(YEAR FROM AGE(CURRENT_DATE, date_of_birth)) AS age,"
              + " gender, ARRAY_TO_STRING(photos, ';', '*') AS photos, description, my_song,"
              + " point(?5, ?6)<@>point(last_longitude, last_latitude) AS distance FROM profiles"
              + " WHERE id != ?2 AND NOT EXISTS (SELECT 1 FROM swipes WHERE receiver_id ="
              + " profiles.id AND sender_id = ?2) AND date_of_birth BETWEEN ?3 AND ?4 AND point(?5,"
              + " ?6)<@>point(last_longitude, last_latitude) < ?7 ORDER BY random() LIMIT 50;",
      nativeQuery = true)
  List<Tuple> findProfilesToSwipe(
      Long userId,
      LocalDate dateOfBirthRangeStart,
      LocalDate dateOfBirthRangeEnd,
      Double longitude,
      Double latitude,
      Integer searchDistance);
}
