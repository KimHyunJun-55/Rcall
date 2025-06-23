package random.call.global.exception.enums;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통 에러
    INVALID_INPUT_VALUE("C001", "잘못된 입력값입니다.", HttpStatus.BAD_REQUEST),

    // 인증/인가 에러
    UNAUTHORIZED("A001", "인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("A002", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 회원 관련 에러
    MEMBER_NOT_FOUND("M001", "회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_MEMBER("M002", "이미 존재하는 회원입니다.", HttpStatus.CONFLICT),
    INVALID_PASSWORD("M003", "잘못된 비밀번호입니다.", HttpStatus.UNAUTHORIZED),

    // 기타 도메인 에러들...
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;
}