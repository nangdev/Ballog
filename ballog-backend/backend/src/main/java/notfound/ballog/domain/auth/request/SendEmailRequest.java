package notfound.ballog.domain.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequest {
    @Email(message = "올바른 이메일을 입력하세요.")
    @NotBlank(message = "이메일을 입력하세요.")
    private String email;
}
