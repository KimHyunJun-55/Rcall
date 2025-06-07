package random.call.domain.reply.dto;

import jakarta.validation.constraints.NotBlank;

public record ReplyUpdateRequest(
        @NotBlank
        String content
) {
}
