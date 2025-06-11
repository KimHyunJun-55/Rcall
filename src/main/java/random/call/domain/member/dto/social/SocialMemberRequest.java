package random.call.domain.member.dto.social;

import jakarta.validation.constraints.NotBlank;
import random.call.domain.member.type.MBTI;
import random.call.domain.member.type.SocialType;

import java.util.List;

public class SocialMemberRequest {
    public record KakaoId(
            @NotBlank
            String kakaoId
    ){

    }

    public record GoogleToken(
            @NotBlank
            String token
    ){

    }

    public record SocialSignUpRequest(

            SocialType socialType,
             String socialId,
             String nickname,
             MBTI mbti,
             String profileImage,
             List<String>interests

    ){

    }

}
