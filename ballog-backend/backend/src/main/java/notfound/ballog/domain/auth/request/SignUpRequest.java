package notfound.ballog.domain.auth.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.auth.entity.Auth;
import notfound.ballog.domain.user.entity.User;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    @Email(message = "올바른 이메일을 입력하세요.")
    @NotBlank(message = "이메일을 입력하세요.")
    private String email;

    @NotBlank(message = "비밀번호를 입력하세요.")
    private String password;

    @NotBlank(message = "닉네임을 입력하세요.")
    private String nickname;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private String profileImageUrl;

    public User toUserEntity(SignUpRequest request) {
        return User.builder()
                .nickname(request.getNickname())
                .birthDate(request.getBirthDate())
                .profileImageUrl(request.getProfileImageUrl())
                .build();
    }

    public Auth toAuthEntity(User user, String email, String password) {
        return Auth.builder()
                .user(user)
                .email(email)
                .password(password)
                .isActive(true)
                .build();
    }
}
