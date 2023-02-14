package millimeeter.server.repository;

import millimeeter.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

  @Transactional
  @Query(value = "INSERT INTO users (id) VALUES (?1) RETURNING id, profile_id;", nativeQuery = true)
  User createUser(String id);

  @Query(value = "SELECT u.profileId FROM users u WHERE u.id = ?1")
  Long findProfileIdById(String id);
}
