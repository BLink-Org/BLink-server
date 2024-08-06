package cmc.blink.global.security.filter;

import cmc.blink.global.exception.JwtAuthenticationException;
import cmc.blink.global.exception.constant.ErrorCode;
import cmc.blink.global.security.provider.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    private static final String[] excludePath = {
            "/swagger", "/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**", "/v3/api-docs/**", "/v3/api-docs/swagger-config",
            "/api/auth/oauth2/login/oauth2/code/google", "/oauth2/authorization/google", "/v3/api-docs",
            "/health", "/favicon.ico"
    };

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        boolean shouldNotFilter = Arrays.stream(excludePath).anyMatch(path::startsWith);

        if (shouldNotFilter) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = tokenProvider.resolveToken(request);

        if (StringUtils.hasText(accessToken) && tokenProvider.verifyToken(accessToken)) {
            Authentication authentication = tokenProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            throw new JwtAuthenticationException(ErrorCode.JWT_TOKEN_NOT_FOUND);
        }

        filterChain.doFilter(request, response);
    }

}
