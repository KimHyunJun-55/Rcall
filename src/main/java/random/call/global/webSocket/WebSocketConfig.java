package random.call.global.webSocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import random.call.global.jwt.JwtUtil;

import java.util.Collections;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;
    private final CustomHandshakeHandler customHandshakeHandler;

    @Bean(name = "customMessageBrokerTaskScheduler")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("wss-heartbeat-thread-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
//                .setAllowedOriginPatterns("https://*.duckdns.org") // 패턴 기반 허용
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(customHandshakeHandler)
                .withSockJS()
                .setStreamBytesLimit(512 * 1024)
                .setDisconnectDelay(30 * 1000);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue", "/topic", "/topic/matching")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(taskScheduler());

        registry.setApplicationDestinationPrefixes("/app", "/app/matching");
        registry.setUserDestinationPrefix("/user"); // 사용자 대상 메시징 활성화
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                String sessionId = accessor.getSessionId();

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // JWT 토큰 검증
                    String token = extractToken(accessor);
                    if (token == null || !jwtUtil.validateToken(token)) {
                        throw new MessagingException("인증 실패");
                    }
                    // 사용자 정보 추출
                    Long memberId = jwtUtil.getMemberIdToToken(token);
                    String nickname = jwtUtil.getNicknameToToken(token);


                    // 세션 속성 저장
                    accessor.getSessionAttributes().put("SESSION_ID", sessionId);
                    accessor.getSessionAttributes().put("id", memberId);
                    accessor.getSessionAttributes().put("user", nickname);


                    log.info("인증 완료: nickname={}, sessionId={}, memberId={}",
                            nickname, sessionId, memberId);
                }

                // 하트비트 메시지 처리


                return message;
            }

            private String extractToken(StompHeaderAccessor accessor) {
                String token = accessor.getFirstNativeHeader("Authorization");
                return (token != null && token.startsWith("Bearer ")) ?
                        token.substring(7) : null;
            }
        });
    }
}