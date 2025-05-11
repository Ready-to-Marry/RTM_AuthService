package ready_to_marry.authservice.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ready_to_marry.authservice.common.dto.response.ApiResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증이 필요할 때(401) JSON 응답을 내려주는 EntryPoint
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper mapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .code(401)
                .message("Unauthorized")
                .data(null)
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream()
                .write(mapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8));
    }
}
