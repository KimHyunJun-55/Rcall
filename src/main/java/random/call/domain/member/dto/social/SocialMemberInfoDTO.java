package random.call.domain.member.dto.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SocialMemberInfoDTO {
    private String id;
    private String nickname;
    private String email;
    private String profileImage;
}