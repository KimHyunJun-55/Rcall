package random.call.global.exception.dto;



import lombok.Builder;
import lombok.Getter;
import random.call.global.exception.enums.ErrorCode;

@Getter
@Builder
public class ErrorResponse {
    private String code;
    private String message;

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
    }
}