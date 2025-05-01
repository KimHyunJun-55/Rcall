package random.call.global.webSocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // CONNECT에서 설정한 Authentication을 Principal로 사용
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth : super.determineUser(request, wsHandler, attributes);
    }
}
