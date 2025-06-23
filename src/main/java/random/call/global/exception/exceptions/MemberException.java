package random.call.global.exception.exceptions;


import random.call.global.exception.enums.ErrorCode;

public class MemberException extends BusinessException {
    public MemberException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MemberException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}