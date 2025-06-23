package random.call.domain.match.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import random.call.domain.match.MatchType;
import random.call.domain.member.type.Gender;
import random.call.global.jwt.JwtUtil;

import java.io.IOException;

@Data
public class MatchRequest {
    private Long userId;
//    private MatchType matchType;
    private int minAge;
    private int maxAge;
    private Gender gender; // Enum 사용
    private String category;


}
