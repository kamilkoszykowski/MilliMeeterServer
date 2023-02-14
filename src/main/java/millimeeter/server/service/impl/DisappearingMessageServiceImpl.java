package millimeeter.server.service.impl;

import millimeeter.server.repository.DisappearingMessageRepository;
import millimeeter.server.service.DisappearingMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DisappearingMessageServiceImpl implements DisappearingMessageService {

  @Autowired private DisappearingMessageRepository disappearingMessageRepository;
}
