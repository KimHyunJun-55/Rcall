package random.call.domain.member.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import random.call.domain.member.Member;

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

    public MemberResponseDTO(Member member){
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.createdAt=member.getCreatedAt();

    }
}
