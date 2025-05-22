package notfound.ballog.exception;

import lombok.Getter;
import notfound.ballog.common.response.BaseResponseStatus;

@Getter
public class ValidationException extends RuntimeException {
    private final BaseResponseStatus status;

    public ValidationException(BaseResponseStatus status) {
        super(status.getMessage());
        this.status = status;
    }
}
