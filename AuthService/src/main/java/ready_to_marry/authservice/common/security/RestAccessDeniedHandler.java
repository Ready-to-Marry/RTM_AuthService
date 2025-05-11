package ready_to_marry.authservice.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import ready_to_marry.authservice.common.dto.response.ApiResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 권한이 부족할 때(403) JSON 응답을 내려주는 Handler
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .code(403)
                .message("Forbidden")
                .data(null)
                .build();

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream()
                .write(mapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8));
    }
}
