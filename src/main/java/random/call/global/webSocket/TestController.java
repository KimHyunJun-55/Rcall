package random.call.global.webSocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Date;

@Controller
public class TestController {

    // Raw WebSocket 테스트
    @GetMapping("/ws-test")
    public String rawWebSocketTest() {
        return "연결 확인 페이지";
    }

    // STOMP 메시지 처리
    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public String handleChat(String message, Message<?> incomingMessage) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(incomingMessage);
        String username = (String) accessor.getSessionAttributes().get("user");  // 세션에서 사용자 이름 꺼내기

        System.out.println("인증된 사용자: " + username);

        // 메시지와 사용자 정보 함께 반환
        return "서버 응답: " + message + " (수신 시간: " + new Date() + ") 사용자: " + username;
    }



}