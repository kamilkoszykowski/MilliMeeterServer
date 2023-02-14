package millimeeter.server.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import millimeeter.server.controller.MatchController;
import millimeeter.server.controller.MessageController;
import millimeeter.server.dto.MatchWithMessagesDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class MatchWithMessagesDtoModelAssembler
    implements RepresentationModelAssembler<
        MatchWithMessagesDto, EntityModel<MatchWithMessagesDto>> {

  @Override
  public EntityModel<MatchWithMessagesDto> toModel(MatchWithMessagesDto match) {
    EntityModel<MatchWithMessagesDto> matchModel = EntityModel.of(match);
    matchModel.add(
        linkTo(methodOn(MessageController.class).findMessagesByMatchId(match.getId()))
            .withRel("conversation"));
    matchModel.add(
        linkTo(methodOn(MatchController.class).deleteMatch(match.getId())).withRel("delete"));

    return matchModel;
  }
}
