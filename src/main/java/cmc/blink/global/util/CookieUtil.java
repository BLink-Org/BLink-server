package cmc.blink.global.util;

import cmc.blink.global.exception.NotFoundException;
import cmc.blink.global.exception.constant.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {
    public Cookie getCookie(HttpServletRequest request, String cookieName) {
        Optional<Cookie> cookie = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(cookieName)).findFirst();
        if(cookie.isPresent()) {
            return cookie.get();
        } else {
            throw new NotFoundException(ErrorCode.COOKIE_NOT_FOUND);
        }
    }

    public Cookie createCookie(String key, String value, int validTime, boolean isHttpOnly) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(validTime);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(isHttpOnly);

        return cookie;
    }
}
