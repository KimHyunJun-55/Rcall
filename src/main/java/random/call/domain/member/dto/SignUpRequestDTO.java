package random.call.domain.member.dto;


import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import random.call.domain.member.type.Gender;
import random.call.domain.member.type.MBTI;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SignUpRequestDTO {

    private String username;
    private String password;
    private String nickname;
    private String phoneNumber;
    private Gender gender;
    @Pattern(regexp = "\\d{8}", message = "생년월일은 8자리 숫자로 입력해주세요")
    private String birthDate;
    private MBTI mbti;
    private String profileImage;
    private List<String> interests;
    private DeviceInfoDTO deviceInfo;



}
