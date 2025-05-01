package random.call.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static random.call.global.jwt.JwtUtil.ACCESS_KEY;
import static random.call.global.jwt.JwtUtil.REFRESH_KEY;


@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String access_token = jwtUtil.resolveToken(request, ACCESS_KEY);
        String refresh_token = jwtUtil.resolveToken(request, REFRESH_KEY);

        if(access_token != null){
            if(jwtUtil.validateToken(access_token)){
                jwtUtil.setAuthentication(access_token);
            }else if(refresh_token != null && jwtUtil.validateToken(refresh_token)){
                jwtUtil.handleRefreshToken(refresh_token, response,request);

            }else{
                jwtExceptionHandler(response, "Invalid Refresh Token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
    public void jwtExceptionHandler(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", msg);
        String jsonResponse = new ObjectMapper().writeValueAsString(responseMap);
        response.getWriter().write(jsonResponse);
    }
}
