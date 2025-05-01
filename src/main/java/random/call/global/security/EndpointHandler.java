package random.call.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class EndpointHandler extends Http403ForbiddenEntryPoint implements AccessDeniedHandler {

    private final EndpointChecker endpointChecker;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        handleRequest(request, response, authException);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        handleRequest(request, response, accessDeniedException);
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response, Exception exception) throws IOException {
        if (!endpointChecker.endpointCheck(request)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "NOT FOUND");
        } else {
            if (exception instanceof AuthenticationException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            } else if (exception instanceof AccessDeniedException) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            }
        }
    }
}