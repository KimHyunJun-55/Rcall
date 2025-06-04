package random.call.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyInterestsResponseDTO {
    List<String> interests;

}
