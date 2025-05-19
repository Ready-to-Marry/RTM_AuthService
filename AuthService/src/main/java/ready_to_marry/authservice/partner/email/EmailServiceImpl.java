package ready_to_marry.authservice.partner.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import ready_to_marry.authservice.common.util.MaskingUtil;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailProperties emailProperties;

    @Override
    @Retryable(
            include = MailException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void sendPartnerVerification(String to, String link) {
        // 1) Thymeleaf 컨텍스트에 변수 설정
        Context ctx = new Context();
        ctx.setVariable("link", link);

        // 2) 템플릿 파일 처리
        String html = templateEngine.process(emailProperties.getTemplates().getPartnerVerification(), ctx);

        // 3) 메일 발송
        sendHtmlMail(to, emailProperties.getFrom(), emailProperties.getPartnerVerificationSubject(), html);
    }

    @Async
    @Override
    @Retryable(
            include = MailException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void sendPartnerApproved(String to) {
        // 1) 템플릿 파일 처리
        String html = templateEngine.process(emailProperties.getTemplates().getPartnerApproved(), new Context());

        // 2) 메일 발송
        sendHtmlMail(to, emailProperties.getFrom(), emailProperties.getPartnerApprovedSubject(), html);
    }

    @Recover
    public void recoverPartnerApproved(MailException ex, String to) {
        log.warn("Failed to send partner approval email: identifierType=loginId, identifierValue={}", MaskingUtil.maskEmailLoginId(to), ex);
    }

    @Async
    @Override
    @Retryable(
            include = MailException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000))
    public void sendPartnerRejected(String to, String reason) {
        // 1) Thymeleaf 컨텍스트에 변수 설정
        Context ctx = new Context();
        ctx.setVariable("reason", reason);

        // 2) 템플릿 파일 처리
        String html = templateEngine.process(emailProperties.getTemplates().getPartnerRejected(), ctx);

        // 3) 메일 발송
        sendHtmlMail(to, emailProperties.getFrom(), emailProperties.getPartnerRejectedSubject(), html);
    }

    @Recover
    public void recoverPartnerRejected(MailException ex, String to, String reason) {
        log.warn("Failed to send partner rejection email: identifierType=loginId, identifierValue={}", MaskingUtil.maskEmailLoginId(to), ex);
    }

    // 공통 HTML 메일 발송 로직
    private void sendHtmlMail(String to, String from, String subject, String html) {
        try {
            // 1) MimeMessage 생성 및 설정
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            // 2) 메일 발송
            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            throw new MailSendException("Failed to send email", ex);
        }
    }
}
