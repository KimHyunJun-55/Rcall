package random.call.domain.member.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import random.call.domain.member.Member;
import random.call.domain.member.type.Gender;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class MemberResponseDTO {

    private Long id;
    private String nickname;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;
    private Gender gender;
    private Integer age;
    private String profileImage;

    public MemberResponseDTO(Member member){
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.gender=member.getGender();
        this.age = member.getAge();
        this.createdAt=member.getCreatedAt();
        this.profileImage=member.getProfileImage();

    }
}
