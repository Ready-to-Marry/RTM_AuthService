package ready_to_marry.authservice.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import ready_to_marry.authservice.common.exception.ErrorCode;
import ready_to_marry.authservice.common.exception.InfrastructureException;

@Slf4j
public final class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JsonUtil() {
        // 유틸 클래스이므로 인스턴스 생성 방지
    }

    /**
     * 객체를 JSON 문자열로 직렬화
     *
     * @throws InfrastructureException JSON_SERIALIZATION_FAILURE
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            log.error("{}: identifierType=class, identifierValue={}", ErrorCode.JSON_SERIALIZATION_FAILURE.getMessage(), obj.getClass().getSimpleName(), ex);
            throw new InfrastructureException(ErrorCode.JSON_SERIALIZATION_FAILURE, ex);
        }
    }
}
