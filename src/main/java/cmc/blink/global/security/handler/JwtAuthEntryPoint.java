package cmc.blink.global.security.handler;

import cmc.blink.global.exception.constant.ErrorCode;
import cmc.blink.global.exception.dto.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final Logger LOGGER = LoggerFactory.getLogger(JwtAuthEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(401);
        PrintWriter writer = response.getWriter();

        try {
            writer.write(ApiErrorResponse.of(ErrorCode.UNAUTHORIZED).toString());
        }catch (NullPointerException e){
            LOGGER.error("JwtAuthEntryPoint 응답 메시지 작성 에러", e);
        }finally {
            if(writer != null){
                writer.flush();
                writer.close();
            }
        }
    }
}