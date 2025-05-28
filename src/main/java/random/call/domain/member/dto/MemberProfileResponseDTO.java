package random.call.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import random.call.domain.member.QuestionAnswer;
import random.call.domain.member.type.Gender;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Builder
@AllArgsConstructor
public class MemberProfileResponseDTO {


    //해당유저정보
    private Long id;
    private String nickname;
    private String profileImage;
    private Gender gender;
    private String location;
    private String statusMessage;
    private List<QuestionsDTO> questionAnswers;

    private List<String> interests;
    private int likes;
    private double temperature;

    //조회요청한 유저와의관계
    @JsonProperty("isFriend")
    private boolean isFriend;
    // 내가 신청했는지
    private boolean friendRequestSentByMe;
    // 상대가 신청했는지
    private boolean friendRequestReceivedFromOther;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionsDTO {
        private String question;
        private String answer;
    }




}
