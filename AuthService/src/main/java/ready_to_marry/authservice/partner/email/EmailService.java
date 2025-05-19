package ready_to_marry.authservice.partner.email;

import org.springframework.mail.MailSendException;

/**
 * HTML 이메일 발송 기능을 제공하는 서비스 인터페이스
 */
public interface EmailService {
    /**
     * 파트너 이메일 인증용 메일 전송
     *
     * @param to    수신자 이메일 주소
     * @param link  인증 링크(URL)
     * @throws MailSendException Failed to send email
     */
    void sendPartnerVerification(String to, String link);

    /**
     * 파트너 계정 승인 안내 메일 전송 (비동기, 실패 시 재시도 후 최종 실패만 로그)
     *
     * @param to    수신자 이메일 주소
     */
    void sendPartnerApproved(String to);

    /**
     * 파트너 계정 거부 안내 메일 전송 (비동기, 실패 시 재시도 후 최종 실패만 로그)
     *
     * @param to     수신자 이메일 주소
     * @param reason 계정 거부 사유
     */
    void sendPartnerRejected(String to, String reason);
}
