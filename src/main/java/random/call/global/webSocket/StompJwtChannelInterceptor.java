package random.call.global.webSocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import random.call.global.jwt.JwtUtil;

@Component
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtil jwtTokenProvider; // 이미 사용 중일 것

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                if (auth != null && auth.isAuthenticated()) {
                    accessor.setUser(auth); // 여기가 핵심
                }
            }
        }
        return message;
    }
}
