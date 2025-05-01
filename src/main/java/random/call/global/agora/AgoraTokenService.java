package random.call.global.agora;


import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AgoraTokenService {

    private static final String APP_ID = "your_app_id";
    private static final String APP_CERTIFICATE = "your_app_certificate";
    private static final int EXPIRE_SECONDS = 3600;

    public String generateToken(String channelName, int uid) {
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();

        int tokenExpire = (int) (Instant.now().getEpochSecond() + EXPIRE_SECONDS); // long을 int로 캐스팅

        return tokenBuilder.buildTokenWithUid(
                APP_ID,
                APP_CERTIFICATE,
                channelName,
                uid,
                RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                tokenExpire, // 여기서 int 타입으로 넘겨줍니다.
                tokenExpire // privilegeExpire도 동일하게 설정
        );
    }

}
