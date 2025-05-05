package random.call.domain.validToken;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/api/auth-token")
@RestController
@Slf4j
public class AuthController {

    // 토큰 유효성 검사 API
    @GetMapping("")
    public ResponseEntity<String> validToken() {
        // 토큰이 유효하다면 200 OK 반환
        log.info("Token is valid");
        return new ResponseEntity<>("Token is valid", HttpStatus.OK);
    }
}
