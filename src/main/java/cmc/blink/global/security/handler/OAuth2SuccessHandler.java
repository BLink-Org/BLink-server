package cmc.blink.global.security.handler;

import cmc.blink.global.security.dto.Token;
import cmc.blink.global.security.provider.TokenProvider;
import cmc.blink.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final CookieUtil cookieUtil;

    @Value("${jwt.token-valid-time}")
    private int tokenValidSeconds;

    @Value("${jwt.refresh-valid-time}")
    private int refreshValidSeconds;

    @Value("${oauth.success-redirect-url}")
    private String successRedirectUrl;

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Token token = tokenProvider.generateToken(authentication);

        response.addCookie(cookieUtil.createCookie("access_token", token.getAccessToken(), tokenValidSeconds, false));
        response.addCookie(cookieUtil.createCookie("refresh_token", token.getRefreshToken(), refreshValidSeconds, true));

        response.sendRedirect(successRedirectUrl);
    }

}
