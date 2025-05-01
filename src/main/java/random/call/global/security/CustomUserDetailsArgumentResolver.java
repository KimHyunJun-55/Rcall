package random.call.global.security;


import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import random.call.global.security.userDetails.CustomUserDetails;
import random.call.global.security.userDetails.JwtUserDetails;
import random.call.global.security.userDetails.UserDetailsServiceImpl;


@Component
@RequiredArgsConstructor
public class CustomUserDetailsArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserDetailsServiceImpl userDetailsService;
    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class) &&
                (parameter.getParameterType().equals(JwtUserDetails.class) ||
                        parameter.getParameterType().equals(CustomUserDetails.class));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        if (authentication.getPrincipal() instanceof JwtUserDetails jwtUserDetails) {
            if (parameter.getParameterType().equals(CustomUserDetails.class)) {
                return userDetailsService.loadUserByUsername(jwtUserDetails.getUsername());
            }
            return jwtUserDetails;
        }

        return authentication.getPrincipal();
    }
}
