package notfound.ballog.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.auth.entity.Auth;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserIdDto {
    private UUID uuid;

    public static UserIdDto of(Auth auth) {
        return UserIdDto.builder()
                .uuid(auth.getUser().getUserId())
                .build();
    }
}
