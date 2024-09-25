package cmc.blink.global.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.springframework.http.HttpStatus;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ApiResponseDto<T> {

    private HttpStatus httpStatus;
    private String message;
    private T result;

    public static<T> ApiResponseDto<T> of (T result){
        return new ApiResponseDto<>(HttpStatus.OK, "요청에 성공하였습니다.", result);
    }

    public static<T> ApiResponseDto<T> of (String message, T result){
        return new ApiResponseDto<>(HttpStatus.OK, message, result);
    }

    public static <T> ApiResponseDto<T> empty(){
        return new ApiResponseDto<>(HttpStatus.OK, "요청에 성공하였습니다.", null);
    }

    public static <T> ApiResponseDto<T> created(String message){
        return new ApiResponseDto<>(HttpStatus.CREATED, message, null);
    }

    public static <T> ApiResponseDto<T> created(String message, T result){
        return new ApiResponseDto<>(HttpStatus.CREATED, message, result);
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
