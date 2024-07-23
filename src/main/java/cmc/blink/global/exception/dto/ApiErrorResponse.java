package cmc.blink.global.exception.dto;

import cmc.blink.global.exception.constant.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.springframework.http.HttpStatus;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ApiErrorResponse {

    private Integer code;
    private HttpStatus httpStatus;
    private String message;

    public static ApiErrorResponse of (ErrorCode errorCode){
        return new ApiErrorResponse(errorCode.getCode(), errorCode.getHttpStatus(), errorCode.getMessage());
    }

    public static ApiErrorResponse of(ErrorCode errorCode, Exception e) {
        return new ApiErrorResponse(errorCode.getCode(), errorCode.getHttpStatus(), errorCode.getMessage(e));
    }

    public static ApiErrorResponse of(ErrorCode errorCode, String message) {
        return new ApiErrorResponse(errorCode.getCode(), errorCode.getHttpStatus(), errorCode.getMessage(message));
    }

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
