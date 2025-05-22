package notfound.ballog.exception;

import lombok.Getter;
import notfound.ballog.common.response.BaseResponseStatus;

@Getter
public class DuplicateDataException extends RuntimeException {
    private final BaseResponseStatus status;

    public DuplicateDataException(BaseResponseStatus status) {
        super(status.getMessage());
        this.status = status;
    }
}
