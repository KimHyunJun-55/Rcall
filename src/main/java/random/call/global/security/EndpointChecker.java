package random.call.global.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;


@Component
@RequiredArgsConstructor
public class EndpointChecker {

    private final ApplicationContext applicationContext;



    public boolean endpointCheck(HttpServletRequest request) {
        DispatcherServlet dispatcherServlet = applicationContext.getBean(DispatcherServlet.class);

        assert dispatcherServlet.getHandlerMappings() != null;

        for (HandlerMapping handlerMapping : dispatcherServlet.getHandlerMappings()) {
            try {
                HandlerExecutionChain foundHandler = handlerMapping.getHandler(request);
                if (foundHandler != null) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}