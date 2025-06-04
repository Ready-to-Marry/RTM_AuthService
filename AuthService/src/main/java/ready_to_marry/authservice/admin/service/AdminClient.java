package ready_to_marry.authservice.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ready_to_marry.authservice.admin.dto.request.AdminProfileRequest;
import ready_to_marry.authservice.common.dto.response.ApiResponse;
import ready_to_marry.authservice.common.exception.ErrorCode;
import ready_to_marry.authservice.common.exception.InfrastructureException;

@Component
@RequiredArgsConstructor
public class AdminClient {

    private final WebClient.Builder webClientBuilder;

    private static final String BASE_URL = "http://admin-service";

    public Long saveAdminProfile(AdminProfileRequest requestDto) {
        System.out.println(BASE_URL + "/internal/admin-id");
        return webClientBuilder.build()
                .post()
                .uri(BASE_URL + "/internal/admin-id")
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                                .flatMap(body -> {
                                    int code = body.getCode();
                                    String message = body.getMessage();

                                    if (code == 2301) {
                                        return Mono.error(new InfrastructureException(ErrorCode.DB_SAVE_FAILURE, new RuntimeException(message)));
                                    } else {
                                        return Mono.error(new RuntimeException("Unknown error: " + message));
                                    }
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Long>>() {})
                .map(ApiResponse::getData)
                .block();
    }
}
