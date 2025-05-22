package notfound.ballog.exception;

import lombok.Getter;
import notfound.ballog.common.response.BaseResponseStatus;

@Getter
public class NotFoundException extends RuntimeException {
    private final BaseResponseStatus status;

    public NotFoundException(BaseResponseStatus status) {
        super(status.getMessage());
        this.status = status;
    }
}
