package cmc.blink.global.security.handler.resolver;

import cmc.blink.domain.user.implement.UserQueryAdapter;
import cmc.blink.domain.user.persistence.Status;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.annotation.AuthUser;
import cmc.blink.global.exception.BadRequestException;
import cmc.blink.global.exception.NotFoundException;
import cmc.blink.global.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserQueryAdapter userQueryAdapter;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(AuthUser.class);
        boolean isUserType = User.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && isUserType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object principal = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            principal = authentication.getPrincipal();
        }

        if (principal == null || principal.getClass() == String.class) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION);
        }

        UserDetails userDetails = (UserDetails) principal;

        String email = userDetails.getUsername();

        User user = userQueryAdapter.findByEmail(email)
                .orElseThrow(()-> new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION));

        if (!(user.getStatus().equals(Status.ACTIVE)))
            throw new BadRequestException(ErrorCode.USER_STATUS_NOT_ACTIVE);

        return user;
    }
}
