package notfound.ballog.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import notfound.ballog.common.response.BaseResponse;
import notfound.ballog.common.response.BaseResponseStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InternalServerException.class)
    public BaseResponse<BaseResponseStatus> InternalServerExceptionHandler(InternalServerException e) {
        log.error("InternalServerException {} {} {}", e.getMessage(), e.getCause(), e.getStackTrace() );
        return BaseResponse.error(e.getStatus());
    }

    @ExceptionHandler(DuplicateDataException.class)
    public BaseResponse<BaseResponseStatus> DuplicateDataExceptionHandler(DuplicateDataException e) {
        log.error("DuplicateDataException {} {} {}", e.getMessage(), e.getCause(), e.getStackTrace() );
        return BaseResponse.error(e.getStatus());
    }

    @ExceptionHandler(ValidationException.class)
    public BaseResponse<BaseResponseStatus> ValidationExceptionHandler(ValidationException e) {
        log.error("ValidationException {} {} {}", e.getMessage(), e.getCause(), e.getStackTrace() );
        return BaseResponse.error(e.getStatus());
    }

    @ExceptionHandler(NotFoundException.class)
    public BaseResponse<BaseResponseStatus> NotFoundExceptionHandler(NotFoundException e) {
        log.error("NotFoundException {} {} {}", e.getMessage(), e.getCause(), e.getStackTrace() );
        return BaseResponse.error(e.getStatus());
    }

    // RequestDto Valid 에러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<Void> MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.error("MethodArgumentNotValidException {}", message );
        return BaseResponse.error(BaseResponseStatus.BAD_REQUEST, message);
    }

    // 쿼리 파라미터 Valid 에러
    @ExceptionHandler(ConstraintViolationException.class)
    public BaseResponse<Void> ConstraintViolationExceptionHandler(ConstraintViolationException e) {
        String message = e.getConstraintViolations().iterator().next().getMessage();
        log.error("ConstraintViolationException {}", message);
        return BaseResponse.error(BaseResponseStatus.BAD_REQUEST, message);
    }
}
