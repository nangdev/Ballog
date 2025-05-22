package notfound.ballog.domain.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import notfound.ballog.domain.user.request.AddS3ImageUrlRequest;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddS3ImageUrlResponse {
    private String s3Url;

    public static AddS3ImageUrlResponse of(String presignedUrl) {
        return AddS3ImageUrlResponse.builder()
                .s3Url(presignedUrl)
                .build();
    }
}
