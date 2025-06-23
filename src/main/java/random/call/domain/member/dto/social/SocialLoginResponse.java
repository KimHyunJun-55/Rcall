package random.call.domain.member.dto.social;

import com.fasterxml.jackson.annotation.JsonFormat;
import random.call.domain.member.type.Gender;

import java.time.LocalDateTime;

public record SocialLoginResponse(
        boolean exists,
        Long id,
        String nickname,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDateTime createdAt,
        Integer age,
        Gender gender,
        Boolean isSubscriber
) {
}
