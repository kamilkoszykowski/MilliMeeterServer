package millimeeter.server.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import millimeeter.server.controller.MatchController;
import millimeeter.server.controller.MessageController;
import millimeeter.server.controller.ProfileController;
import millimeeter.server.model.Match;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class MatchModelAssembler
    implements RepresentationModelAssembler<Match, EntityModel<Match>> {

  @Override
  public EntityModel<Match> toModel(Match match) {
    EntityModel<Match> matchModel = EntityModel.of(match);
    matchModel.add(
        linkTo(methodOn(MessageController.class).findMessagesByMatchId(match.getId()))
            .withRel("conversation"));
    matchModel.add(linkTo(methodOn(MessageController.class).send(null)).withRel("send message"));
    matchModel.add(
        linkTo(methodOn(MatchController.class).deleteMatch(match.getId())).withRel("delete"));
    matchModel.add(
        linkTo(methodOn(ProfileController.class).getSwipesLeftCount())
            .withRel("swipes left amount"));

    return matchModel;
  }
}
