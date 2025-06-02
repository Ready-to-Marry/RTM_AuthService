package ready_to_marry.authservice.partner.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ready_to_marry.authservice.common.exception.BusinessException;
import ready_to_marry.authservice.common.exception.InfrastructureException;
import ready_to_marry.authservice.partner.service.PartnerAuthService;

@Controller
@RequestMapping("/auth/partners/verify")
@RequiredArgsConstructor
public class PartnerAuthWebController {
    private final PartnerAuthService partnerAuthService;

    /**
     * 이메일 인증 처리: 링크 클릭 시 호출
     *
     * @param token 이메일 인증 토큰
     * @param attrs  리디렉트 시 보여줄 플래시 속성을 담는 객체
     *               status = "success" : 인증 성공
     *               status = "expired" : 유효하지 않은 토큰(만료)
     *               status = "retry"   : DB/Redis 장애 발생
     *               status = "error"   : 기타 예기치 못한 예외 발생
     * @return {@code "redirect:/auth/partners/verify/result"} 인증 결과 페이지로의 리디렉션 문자열
     */
    @GetMapping
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes attrs) {
        try {
            // 인증 토큰 검증 및 auth_account(authDB) status 값 수정
            partnerAuthService.verifyEmail(token);
            attrs.addFlashAttribute("status", "success");
        } catch (BusinessException ex) {
            attrs.addFlashAttribute("status", "expired");
        } catch (InfrastructureException ex) {
            attrs.addFlashAttribute("status", "retry");
        } catch (Exception ex) {
            attrs.addFlashAttribute("status", "error");
        }

        return "redirect:/auth-service/auth/partners/verify/result";
    }

    /**
     * 인증 결과 페이지 렌더링
     *
     * 플래시 속성(status)에 따라 뷰 내에서 성공/실패 메시지를 분기하여 보여줌
     * template 위치: src/main/resources/templates/partner-verification-result.html
     *
     * @return 뷰 이름 ("partner-verification-result")
     */
    @GetMapping("/result")
    public String showVerifyEmailResult() {
        return "partner-verification-result";
    }
}
