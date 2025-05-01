package random.call.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

public class MemberRequest {

    public record CheckNickname(
            @NotBlank
            String nickname
    ){

    }
    public record CheckUsername(
            @NotBlank
            String username
    ){

    }
}
