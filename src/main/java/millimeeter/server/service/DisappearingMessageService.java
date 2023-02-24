package millimeeter.server.service;

import millimeeter.server.repository.DisappearingMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DisappearingMessageService {

  private final DisappearingMessageRepository disappearingMessageRepository;

  @Autowired
  public DisappearingMessageService(DisappearingMessageRepository disappearingMessageRepository) {
    this.disappearingMessageRepository = disappearingMessageRepository;
  }
}
