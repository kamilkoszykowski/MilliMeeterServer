package millimeeter.server.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import millimeeter.server.controller.MessageController;
import millimeeter.server.dto.MessageDto;
import millimeeter.server.enums.MessageReaction;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class MessageDtoModelAssembler
    implements RepresentationModelAssembler<MessageDto, EntityModel<MessageDto>> {

  @Override
  public EntityModel<MessageDto> toModel(MessageDto message) {
    EntityModel<MessageDto> messageModel = EntityModel.of(message);
    messageModel.add(linkTo(methodOn(MessageController.class).send(null)).withRel("reply"));
    messageModel.add(
        linkTo(
                methodOn(MessageController.class)
                    .reactToMessage(message.getId(), MessageReaction.LIKE.toString()))
            .withRel("react like"));
    messageModel.add(
        linkTo(
                methodOn(MessageController.class)
                    .reactToMessage(message.getId(), MessageReaction.SUPER.toString()))
            .withRel("react super"));
    messageModel.add(
        linkTo(
                methodOn(MessageController.class)
                    .reactToMessage(message.getId(), MessageReaction.HAHA.toString()))
            .withRel("react haha"));
    messageModel.add(
        linkTo(
                methodOn(MessageController.class)
                    .reactToMessage(message.getId(), MessageReaction.CRY.toString()))
            .withRel("react cry"));
    messageModel.add(
        linkTo(
                methodOn(MessageController.class)
                    .reactToMessage(message.getId(), MessageReaction.WRR.toString()))
            .withRel("react wrr"));
    messageModel.add(
        linkTo(
                methodOn(MessageController.class)
                    .reactToMessage(message.getId(), MessageReaction.CARE.toString()))
            .withRel("react care"));
    messageModel.add(
        linkTo(methodOn(MessageController.class).deleteReactionFromMessage(message.getId()))
            .withRel("delete reaction"));

    return messageModel;
  }
}
