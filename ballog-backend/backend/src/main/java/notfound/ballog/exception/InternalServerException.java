package notfound.ballog.exception;

import lombok.Getter;
import notfound.ballog.common.response.BaseResponseStatus;

@Getter
public class InternalServerException extends RuntimeException {
    private final BaseResponseStatus status;

    public InternalServerException(BaseResponseStatus status) {
        super(status.getMessage());
        this.status = status;
    }

}
