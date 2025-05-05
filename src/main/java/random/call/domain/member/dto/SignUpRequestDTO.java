package random.call.domain.member.dto;


import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    private Gender gender;
    private MBTI mbti;
    private String profileImage;
    private List<String> interests;

}
