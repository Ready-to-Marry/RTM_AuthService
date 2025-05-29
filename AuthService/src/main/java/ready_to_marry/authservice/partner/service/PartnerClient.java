package ready_to_marry.authservice.partner.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ready_to_marry.authservice.common.dto.response.ApiResponse;
import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.ErrorCode;
import ready_to_marry.authservice.common.exception.InfrastructureException;
import ready_to_marry.authservice.partner.dto.request.PartnerProfileRequest;
import ready_to_marry.authservice.partner.dto.request.PartnerResponseDto;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class PartnerClient {

    private final WebClient webClient;
    private final String partnerServiceUrl = "http://partner-service";


    public PartnerResponseDto getPartnerProfile(Long partnerId) {
        return webClient.get()
                .uri(partnerServiceUrl + "/partner/profile/{partnerId}", partnerId)
                .retrieve()
                .onStatus(status -> status.isError(), (Function<ClientResponse, Mono<? extends Throwable>>) response ->
                        response.bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                                .flatMap((Function<ApiResponse<Void>, Mono<? extends Throwable>>) body -> {
                                    int code = body.getCode();
                                    String message = body.getMessage();

                                    if (code == 1501) {
                                        return Mono.error(new BusinessException(ErrorCode.DUPLICATE_BUSINESS_NUM));
                                    } else if (code == 1504) {
                                        return Mono.error(new BusinessException(ErrorCode.PARTNER_NOT_FOUND));
                                    } else if (code == 1502 || code == 1503) {
                                        return Mono.error(new BusinessException(ErrorCode.NO_SEARCH_TERM));
                                    } else {
                                        return Mono.error(new RuntimeException("Unknown error: " + message));
                                    }
                                })
                )


                .bodyToMono(new ParameterizedTypeReference<ApiResponse<PartnerResponseDto>>() {})
                .map(ApiResponse::getData)
                .block();
    }

    public void deletePartnerProfile(Long partnerId) {
        webClient.delete()
                .uri(partnerServiceUrl + "/delete/{partnerId}", partnerId)
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                                .flatMap(body -> {
                                    int code = body.getCode();
                                    String message = body.getMessage();

                                    // 에러 코드에 따라 커스텀 예외 던지기
                                    if (code == 1504) {
                                        return Mono.error(new BusinessException(ErrorCode.PARTNER_NOT_FOUND));
                                    } else {
                                        return Mono.error(new RuntimeException("Unknown error: " + message));
                                    }
                                })
                )
                .bodyToMono(Void.class)
                .block();
    }

    public Long savePartnerProfile(PartnerProfileRequest requestDto) {
        Long partnerId = webClient.post()
                .uri(partnerServiceUrl + "/register")
                .bodyValue(requestDto)
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                                .flatMap(body -> {
                                    int code = body.getCode();
                                    String message = body.getMessage();

                                    if (code == 2501) {
                                        return Mono.error(new InfrastructureException(ErrorCode.DB_SAVE_FAILURE, new RuntimeException(message)));
                                    } else {
                                        return Mono.error(new RuntimeException("Unknown error: " + message));
                                    }
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Long>>() {})  // ← 여기!
                .map(ApiResponse::getData)
                .block();

        return partnerId;
    }
}
