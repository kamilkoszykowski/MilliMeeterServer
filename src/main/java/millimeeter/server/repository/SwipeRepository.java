package millimeeter.server.repository;

import millimeeter.server.enums.SwipeDirection;
import millimeeter.server.model.Swipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SwipeRepository extends JpaRepository<Swipe, Long> {

  @Query(
      "SELECT COUNT(s) FROM swipes s "
          + "WHERE s.senderId = ?1 "
          + "AND s.receiverId = ?2 "
          + "AND s.direction = ?3 "
          + "OR "
          + "s.senderId = ?2 "
          + "AND s.receiverId = ?1 "
          + "AND s.direction = ?3")
  int countSelectedSwipesBetween(Long profileId1, Long profileId2, SwipeDirection swipeDirection);

  @Query(
      "SELECT COUNT(s) FROM swipes s "
          + "WHERE s.senderId = ?1 "
          + "AND s.receiverId = ?2 "
          + "OR "
          + "s.senderId = ?2 "
          + "AND s.receiverId = ?1")
  int countAllSwipesBetween(Long profileId1, Long profileId2);

  @Query(
      value =
          "SELECT COUNT(s) FROM swipes s "
              + "WHERE s.senderId = ?1 "
              + "AND s.receiverId = ?2 "
              + "AND s.direction = ?3")
  int countSelectedSwipesFromSenderToReceiver(
      Long senderId, Long receiverId, SwipeDirection swipeDirection);

  @Query(
      value = "SELECT COUNT(s) FROM swipes s " + "WHERE s.senderId = ?1 " + "AND s.receiverId = ?2")
  int countAllSwipesFromSenderToReceiver(Long senderId, Long receiverId);
}
