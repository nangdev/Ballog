package notfound.ballog.domain.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;

    @NotNull
    private LocalDate birthDate;

    private String profileImageUrl;
}
