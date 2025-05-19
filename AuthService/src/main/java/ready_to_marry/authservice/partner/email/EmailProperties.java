package ready_to_marry.authservice.partner.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 메일 전송 관련 설정 바인딩 클래스
 *
 * - app.mail.* 프로퍼티를 매핑
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.mail")
public class EmailProperties {
    // 발신자 주소
    private String from;

    // 파트너 인증 메일 제목
    private String partnerVerificationSubject;

    // 파트너 계정 승인 안내 메일 제목
    private String partnerApprovedSubject;

    // 파트너 계정 거부 안내 메일 제목
    private String partnerRejectedSubject;

    // 템플릿 파일명 (classpath:/templates/)
    private Templates templates = new Templates();

    @Getter
    @Setter
    public static class Templates {
        // 파트너 인증 이메일 템플릿 파일명
        private String partnerVerification;

        // 파트너 계정 승인 안내 템플릿 파일명
        private String partnerApproved;

        // 파트너 계정 거부 안내 템플릿 파일명
        private String partnerRejected;
    }
}
