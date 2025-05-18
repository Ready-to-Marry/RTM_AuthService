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
}
