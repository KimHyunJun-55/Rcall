package random.call.global.webSocket;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import random.call.global.jwt.JwtUtil;
import random.call.global.jwt.StompJwtAuth;
import random.call.global.jwt.TokenDto;

import static random.call.global.jwt.JwtUtil.ACCESS_KEY;
import static random.call.global.jwt.JwtUtil.REFRESH_KEY;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;
    private final CustomHandshakeHandler customHandshakeHandler;


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
                registry.addEndpoint("/ws-stomp") // SockJS용
                .setAllowedOriginPatterns("*")
                        .setHandshakeHandler(customHandshakeHandler) // 주입된 핸들러 사용

//                .setAllowedOrigins("http://localhost:8080") // 정확한 origin
                .withSockJS();
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                String sessionId2 = accessor.getSessionId();  // 세션 ID 추출
                System.out.println(sessionId2);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // 세션 ID 추출
                    String sessionId = accessor.getSessionId();

                    // JWT 토큰 추출 및 유효성 검사
                    String token = accessor.getFirstNativeHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    }

                    if (token == null || !jwtUtil.validateToken(token)) {
                        throw new MessagingException("인증 실패");
                    }

                    String nickname = jwtUtil.getNicknameToToken(token);

                    accessor.getSessionAttributes().put("SESSION_ID", sessionId);  // 세션 ID 저장
                    accessor.getSessionAttributes().put("user", nickname);  // 유저명 저장

                    System.out.println("인증 완료: " + nickname + ", 세션 ID: " + sessionId);
                }

                return message;
            }
        });
    }


}
