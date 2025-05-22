package notfound.ballog.common.utils;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import notfound.ballog.common.response.BaseResponseStatus;
import notfound.ballog.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class S3Util {

    private final AmazonS3 amazonS3;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${aws.presign.expirationMinutes}")
    private int expirationMinutes;

    // presignedUrl 생성
    public String generatePresignedUrl(String objectKey, String contentType) {
        try {
            Instant expirationInstant = Instant.now().plusSeconds(expirationMinutes * 60L);
            Date expiration = Date.from(expirationInstant);

            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, objectKey)
                    .withMethod(HttpMethod.PUT)
                    .withExpiration(expiration);

            request.setContentType(contentType);

            URL url = amazonS3.generatePresignedUrl(request);

            return url.toString();

        } catch (Exception e) {
            throw new InternalServerException(BaseResponseStatus.URL_GENERATION_FAIL);
        }
    }

    // objectKey 조합
    public String generateObjectKey(String originalFileName, String domain) {
        // 1. 확장자 추출
        String ext = "";
        int idx = originalFileName.lastIndexOf(".");
        if (idx > 0) {
            ext = originalFileName.substring(idx);
        }

        // 2. 날짜 경로 생성
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 3. UUID 생성
        String uuid = UUID.randomUUID().toString();

        // 4. objectKey 조합
        return String.join("/",
                domain,
                datePath,
                uuid + ext
        );
    }
}
