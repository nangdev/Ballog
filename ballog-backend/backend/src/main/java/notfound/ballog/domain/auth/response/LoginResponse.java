package notfound.ballog.domain.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.auth.dto.JwtTokenDto;
import notfound.ballog.domain.user.dto.UserIdDto;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String accessToken;

    private String refreshToken;

    private UserIdDto user;

    public static LoginResponse of(JwtTokenDto token, UserIdDto userIdDto) {
        return LoginResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .user(userIdDto)
                .build();
    }
}
