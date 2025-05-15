package ready_to_marry.authservice.partner.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

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
        try {
            // 1) Thymeleaf 컨텍스트에 변수 설정
            Context ctx = new Context();
            ctx.setVariable("link", link);

            // 2) 템플릿 파일 처리
            String templateName = emailProperties.getTemplates().getPartnerVerification();
            String html = templateEngine.process(templateName, ctx);

            // 3) MimeMessage 생성 및 설정
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(emailProperties.getFrom());
            helper.setTo(to);
            helper.setSubject(emailProperties.getPartnerVerificationSubject());
            helper.setText(html, true);

            // 4) 메일 발송
            mailSender.send(message);
        } catch (MessagingException | RuntimeException ex) {
            // 템플릿 처리나 발송 중 오류 포함
            throw new MailSendException("Failed to send email", ex);
        }
    }
}
