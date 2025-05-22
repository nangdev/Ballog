package notfound.ballog.common.response;

import lombok.Getter;

@Getter
public class BaseResponse<T> {

    private final Boolean isSuccess;

    private final int code;

    private String message;

    private final T result;

    // 성공 응답 생성자
    private BaseResponse(T result) {
        this.isSuccess = BaseResponseStatus.SUCCESS.isSuccess();
        this.code = BaseResponseStatus.SUCCESS.getCode();
        this.message = BaseResponseStatus.SUCCESS.getMessage();
        this.result = result;
    }

    // 커스텀 상태 응답 생성자
    private BaseResponse(BaseResponseStatus status, T result) {
        this.isSuccess = status.isSuccess();
        this.code = status.getCode();
        this.message = status.getMessage();
        this.result = result;
    }

    // 데이터 없는 성공 응답
    public static <T> BaseResponse<T> ok() {
        return new BaseResponse<>(null);
    }

    // 데이터 있는 성공 응답
    public static <T> BaseResponse<T> ok(T result) {
        return new BaseResponse<>(result);
    }

    // 에러 응답
    public static <T> BaseResponse<T> error(BaseResponseStatus status) {
        return new BaseResponse<>(status, null);
    }

    // 커스텀 메시지 에러 응답
    public static <T> BaseResponse<T> error(BaseResponseStatus status, String message) {
        BaseResponse<T> response = new BaseResponse<>(status, null);
        response.message = message;
        return response;
    }

}
