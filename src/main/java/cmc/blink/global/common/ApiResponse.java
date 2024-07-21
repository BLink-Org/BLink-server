package cmc.blink.global.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.springframework.http.HttpStatus;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ApiResponse<T> {

    private HttpStatus httpStatus;
    private String message;
    private T result;

    public static<T> ApiResponse<T> of (T result){
        return new ApiResponse<>(HttpStatus.OK, "요청에 성공하였습니다.", result);
    }

    public static<T> ApiResponse<T> of (String message, T result){
        return new ApiResponse<>(HttpStatus.OK, message, result);
    }

    public static <T> ApiResponse<T> empty(){
        return new ApiResponse<>(HttpStatus.OK, "요청에 성공하였습니다.", null);
    }

    public static <T> ApiResponse<T> created(String message){
        return new ApiResponse<>(HttpStatus.CREATED, message, null);
    }

    public static <T> ApiResponse<T> created(String message, T result){
        return new ApiResponse<>(HttpStatus.CREATED, message, result);
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
