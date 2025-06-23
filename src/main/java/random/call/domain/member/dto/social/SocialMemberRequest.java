package random.call.domain.member.dto.social;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import random.call.domain.member.type.Gender;
import random.call.domain.member.type.MBTI;
import random.call.domain.member.type.SocialType;

import java.util.List;

public class SocialMemberRequest {
    public record SocialInfo(
            @NotBlank
            String socialId,
            SocialType socialType
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
            @Pattern(regexp = "\\d{8}", message = "생년월일은 8자리 숫자로 입력해주세요")
             String birthDate,
             String phoneNumber,
             Gender gender,
             MBTI mbti,
             String profileImage,
             List<String>interests

    ){

    }

}
