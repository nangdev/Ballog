package notfound.ballog.domain.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.user.entity.User;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetUserResponse {

    private String email;

    private String gender;

    private String nickname;

    private LocalDate birthDate;

    private String profileImageUrl;

    public static GetUserResponse of(User user, String email) {
        return GetUserResponse.builder()
                .email(email)
                .gender(user.getGender())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
