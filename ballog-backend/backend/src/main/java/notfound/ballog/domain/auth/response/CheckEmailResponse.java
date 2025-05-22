package notfound.ballog.domain.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckEmailResponse {
    private boolean isValid;

    public static CheckEmailResponse of(Boolean isExist) {
        return CheckEmailResponse.builder()
                .isValid(isExist)
                .build();
    }
}
