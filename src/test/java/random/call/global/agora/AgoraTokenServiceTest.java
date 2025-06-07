package random.call.global.agora;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

class AgoraTokenServiceTest {

    @Test
    void generateTokenTest() {
        // 가짜 프로퍼티 설정
        AgoraProperties properties = new AgoraProperties();
        properties.setAppId("3d026");
        properties.setAppCertificate("281ea");
        properties.setExpireSeconds(3600);

        // 서비스에 주입
        AgoraTokenService callTokenService = new AgoraTokenService(properties);

        // 테스트 실행
        String token = callTokenService.generateToken("testChannel", 1);
        System.out.println("🔐 Generated Token: " + token);
        assert token != null && token.length() > 50;
    }

}

