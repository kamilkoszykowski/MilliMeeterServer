package millimeeter.server.dto;

import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SentMessageDto {

  @NotNull(message = "The match id is required")
  @Positive(message = "The match id must be a positive number")
  private Long matchId;

  @NotBlank(message = "The content is required")
  @Size(min = 1, max = 1000, message = "The message content must be between 1 and 1000 characters")
  private String content;

  @Positive(message = "The parent message id must be a positive number")
  private Long parentMessageId;
}
