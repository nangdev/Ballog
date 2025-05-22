package notfound.ballog.common.response;

import lombok.Getter;

@Getter
public enum BaseResponseStatus {

    // 성공 응답
    SUCCESS(true, 200, "요청에 성공하였습니다."),

    // 클라이언트 에러 (400번대)
    BAD_REQUEST(false, 400, "잘못된 요청입니다."),
    UNAUTHORIZED(false, 401, "인증이 필요합니다."),
    FORBIDDEN(false, 403, "접근 권한이 없습니다."),
    NOT_FOUND(false, 404, "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(false, 405, "허용되지 않는 메서드입니다."),

    // 서버 에러 (500번대)
    INTERNAL_SERVER_ERROR(false, 500, "서버 내부 오류가 발생했습니다."),
    DATABASE_ERROR(false, 501, "데이터베이스 오류가 발생했습니다."),

    // 회원 로직 관련 에러 (1000번대)
    INVALID_EMAIL(false, 1001, "유효하지 않은 이메일입니다."),
    DUPLICATE_EMAIL(false, 1002, "이미 사용 중인 이메일입니다."),
    PASSWORD_MISMATCH(false, 1003, "비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(false, 1004, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(false, 1005, "토큰이 만료되었습니다."),
    MISSING_TOKEN(false, 1006, "요청에 토큰이 존재하지 않습니다."),
    EMAIL_AUTH_CODE_SEND_FAIL(false, 1007, "이메일 확인 코드 전송 중 오류가 발생했습니다."),
    EXPIRED_EMAIL_AUTH_CODE(false, 1008, "이메일 인증 코드가 없거나 만료되었습니다."),
    INVALID_EMAIL_AUTH_CODE(false, 1009, "유효하지 않은 이메일 인증 코드입니다."),
    RECOMMAND_PLAYER_GPT_ERROR(false, 1010, "OpenAI API 호출 중 오류 발생"),


    // 유저 관련 로직 에러 (2000번대)
    USER_NOT_FOUND(false, 2000, "해당하는 사용자가 존재하지 않습니다."),
    PLAYER_CARD_NOT_FOUND(false, 2001, "해당하는 사용자의 선수카드가 존재하지 않습니다."),
    USER_INACTIVE(false, 2002, "탈퇴한 사용자입니다."),

    // 매치 관련 로직 에러(3000번대)
    MATCH_NOT_FOUND(false, 3000, "해당하는 매치가 존재하지 않습니다."),

    // 영상 관련 로직 에러(4000번대)
    VIDEO_NOT_FOUND(false, 4000, "해당 경기에 업로드 된 영상이 없습니다."),
    HIGHLIGHT_NOT_FOUND(false, 4001, "해당 영상에 하이라이트 영상이 없습니다."),
    URL_GENERATION_FAIL(false, 4002, "영상 업로드 url 생성에 실패했습니다."),
    VIDEO_ALREADY_EXIST(false, 4003, "이미 업로드된 영상이 있습니다."),
    HIGHLIGHT_ALREADY_EXIST(false, 4004, "이미 하이라이트를 자동 추출하였습니다."),
    HIGHLIGHT_EXTRACT_FAIL(false, 4005, "하이라이트 추출에 실패했습니다."),

    // 팀 관련 로직 에러 (5000번대)
    TEAM_NOT_FOUND(false, 5000, "해당하는 팀이 존재하지 않습니다."),
    TEAMMEMBER_NOT_FOUND(false, 5001, "해당하는 팀 멤버가 존재하지 않습니다."),
    TEAMMEMBER_NOT_AUTHORIZED(false, 5002, "매니저만 할 수 있는 기능입니다"),
    TEAM_NOT_EMPTY(false, 5003, "팀이 삭제 되려면 팀에 팀원이 존재하지 않아야 합니다."),
    TEAM_DELETE_ERROR(false, 5004, "팀 삭제 중 에러가 발생했습니다."),

    // 쿼터 관련 로직 에러(6000번대)
    QUARTER_MATCH_NOT_FOUND(false, 6000, "해당 날짜에 매치가 존재하지 않습니다."),
    GAME_REPORT_NOT_FOUND(false, 6001, "최근 매치가 존재하지 않습니다");


    private final boolean isSuccess;

    private final int code;

    private final String message;

    BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }

}