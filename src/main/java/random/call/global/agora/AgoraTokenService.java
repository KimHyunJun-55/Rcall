
package random.call.global.agora;


import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AgoraTokenService {

    private final AgoraProperties properties;

    public AgoraTokenService(AgoraProperties properties) {
        this.properties = properties;
    }
    public String generateToken(String channelName, int userIdStr) {
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();

        int expire = (int) (Instant.now().getEpochSecond() + properties.getExpireSeconds());
        System.out.println("getAppCertificate"+properties.getAppCertificate());
        System.out.println("getExpireSeconds"+properties.getExpireSeconds());
        System.out.println("getAppId"+properties.getAppId());

        return tokenBuilder.buildTokenWithUid(
                properties.getAppId(),
                properties.getAppCertificate(),
                channelName,
                userIdStr,
                RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                expire,
                expire
        );
    }

}
