package millimeeter.server.repository;

import millimeeter.server.model.DisappearingMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisappearingMessageRepository extends JpaRepository<DisappearingMessage, Long> {}
