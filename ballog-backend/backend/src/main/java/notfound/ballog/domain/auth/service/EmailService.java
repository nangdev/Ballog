package notfound.ballog.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notfound.ballog.common.response.BaseResponseStatus;
import notfound.ballog.domain.auth.request.SendEmailRequest;
import notfound.ballog.domain.auth.request.VerifyEmailRequest;
import notfound.ballog.exception.InternalServerException;
import notfound.ballog.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.mail.auth-code-expiration-ms}")
    private Integer timeOutMs;

    @Transactional
    public void sendEmailCode(SendEmailRequest request) {
        String email = request.getEmail();

        // 1. 랜덤 6자리 숫자 코드 생성
        String authCode = String.valueOf(ThreadLocalRandom.current().nextInt(100_000, 1_000_000));

        // 2. Redis에 저장 (키: verify:email:{email}, 값: code, TTL)
        String key = "verify:email:" + email;

        redisTemplate.opsForValue().set(key, authCode, Duration.ofMillis(timeOutMs));

        // 3) HTML 메일 전송을 위해 MimeMessage 사용
        try {
            MimeMessage mime = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mime, "UTF-8");

            helper.setTo(email);

            helper.setSubject("[Ballog] 이메일 인증 코드");

            // HTML 본문
            StringBuilder html = new StringBuilder();
            html.append("<html><body>")
                    .append("<h3>요청하신 인증 번호입니다.</h3>")
                    .append("<div style=\"font-size:24px;font-weight:bold;\">")
                    .append(authCode)
                    .append("</div>")
                    .append("<p>5분 이내에 입력해주세요.</p>")
                    .append("</body></html>");

            helper.setText(html.toString(), true);

            javaMailSender.send(mime);

        } catch (MessagingException | MailException e) {
            log.error("메일 전송/생성 실패", e);

            throw new ValidationException(BaseResponseStatus.EMAIL_AUTH_CODE_SEND_FAIL);
        } catch (Exception e) {
            log.error("sendEmailCode 실패, key={}, timeoutMs={}", key, timeOutMs, e);

            throw new InternalServerException(BaseResponseStatus.EMAIL_AUTH_CODE_SEND_FAIL);
        }
    }

    @Transactional
    public void verifyEmailCode(VerifyEmailRequest request) {
        String email = request.getEmail();

        String authCode = request.getAuthCode();

        String key = "verify:email:" + email;

        // 1. Redis에서 코드 조회
        String savedAuthCode = redisTemplate.opsForValue().get(key);

        // 2. 코드 만료 여부 체크
        if (savedAuthCode == null) {
            throw new ValidationException(BaseResponseStatus.EXPIRED_EMAIL_AUTH_CODE);
        }

        // 3. 클라이언트가 보낸 코드와 비교
        if (!savedAuthCode.equals(authCode)) {
            throw new ValidationException(BaseResponseStatus.INVALID_EMAIL_AUTH_CODE);
        }

        // 4. 같을 경우 Redis 키 삭제
        redisTemplate.delete(key);
    }
}
