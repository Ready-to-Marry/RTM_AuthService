package ready_to_marry.authservice.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ready_to_marry.authservice.common.dto.response.ApiResponse;
import ready_to_marry.authservice.common.exception.ErrorCode;
import ready_to_marry.authservice.common.exception.InfrastructureException;
import ready_to_marry.authservice.user.dto.request.UserProfileRequest;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final WebClient.Builder webClientBuilder;

    private static final String BASE_URL = "http://user-service";

    public Long savePartnerProfile(UserProfileRequest requestDto) {
        System.out.println(requestDto);
        System.out.println(BASE_URL + "/internal/user-profiles");
        return webClientBuilder.build()
                .post()
                .uri(BASE_URL + "/internal/user-profiles")
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                                .flatMap(body -> {
                                    int code = body.getCode();
                                    String message = body.getMessage();
                                    System.out.println(BASE_URL + "/internal/user-profiles");

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
