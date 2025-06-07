package random.call.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import random.call.domain.member.type.MBTI;

import java.util.List;

public class MemberRequest {

    public record CheckNickname(
            @NotBlank
            String nickname
    ){

    }
    public record Mbti(
            @NotBlank
            MBTI mbti
    ){

    }
    public record Message(
            @NotBlank
            String message
    ){

    }
    public record ProfileImage(
            @NotBlank
            String imageUrl
    ){

    }
    public record CheckUsername(
            @NotBlank
            String username
    ){

    }

    public record MemberInfo(
            String nickname,
            String statusMessage,
            String profileImage
    ) {}

    public record MemberInterests(
            List<String> interests
    ) {}


    public record QuestionAnswerRequest(
            String question,
            String answer
    ) {}
    public record QuestionAnswerRequests(
            List<QuestionAnswerRequest> questions
    ) {}


}
