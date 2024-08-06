package cmc.blink.global.security.client;

import cmc.blink.global.exception.FeignException;
import cmc.blink.global.exception.constant.ErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class GoogleFeignError implements ErrorDecoder {

    Logger logger = LoggerFactory.getLogger(GoogleFeignError.class);

    @Override
    public Exception decode(String methodKey, Response response) {
        if (400 <= response.status() && response.status() <= 499){
            logger.error("Google Feign Client 400번대 에러 발생 : {}", response.reason());
            return new FeignException(ErrorCode.GOOGLE_FEIGN_CLIENT_ERROR_400);
        }else {
            logger.error("Google Feign Client 500번대 에러 발생 : {}", response.reason());
            return new FeignException(ErrorCode.GOOGLE_FEIGN_CLIENT_ERROR_500);
        }
    }
}
