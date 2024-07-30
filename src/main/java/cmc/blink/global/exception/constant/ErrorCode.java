package cmc.blink.global.exception.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Error code 목록
 *
 * <ul>
 *     <li>1001 ~ 1999: 일반 예외. </li>
 *     <li>2000 ~ 2199: 인증 관련 예외</li>
 *     <li>2200 ~ 2399: 유저 관련 예외</li>
 *     <li>2400 ~ 2599: 폴더 관련 예외</li>
 *     <li>2600 ~ 2799: 링크 관련 예외</li>
 *     <li>2800 ~ 2999: 북마크 관련 예외</li>
 * </ul>
 */

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {

    FORBIDDEN(HttpStatus.FORBIDDEN, 1001, "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 1002,"인증 정보가 유효하지 않습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, 1003,"잘못된 요청 입니다."),
    INVALID_DATA(HttpStatus.BAD_REQUEST, 1004, "요청 데이터에 대한 유효성 검사 실패입니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, 1005, "요청 파라미터가 잘못 되었습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1006, "서버 내부 에러입니다."),

    // 인증 관련 에러
    INVALID_OAUTH2_PROVIDER(HttpStatus.UNAUTHORIZED, 2000, "유효하지 않은 OAuth2 프로바이더 요청입니다."),
    COOKIE_NOT_FOUND(HttpStatus.NOT_FOUND, 2001, "쿠키가 존재하지 않습니다."),
    INVALID_REFRESH_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, 2002, "유효하지 않은 리프레시 토큰입니다."),
    BLACKLISTED_ACCESSTOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, 2003, "블랙리스트에 등록된 토큰입니다."),
    JWT_BAD_REQUEST(HttpStatus.UNAUTHORIZED, 2004, "잘못된 JWT 서명입니다."),
    JWT_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 2005, "만료된 JWT 토큰입니다."),
    JWT_UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, 2006,"지원하지 않는 JWT 토큰입니다."),
    JWT_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, 2007, "토큰을 찾을 수 없습니다."),

    // 유저 관련 에러
    USER_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, 2200, "사용자를 찾을 수 없습니다."),


    // 폴더 관련 에러
    DUPLICATE_FOLDER_TITLE(HttpStatus.BAD_REQUEST, 2400, "이미 존재하는 폴더 제목입니다."),
    FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, 2401, "폴더를 찾을 수 없습니다."),
    FOLDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, 2402, "해당 폴더에 대한 접근이 거부되었습니다."),

    // 링크 관련 에러
    DUPLICATE_LINK_URL(HttpStatus.BAD_REQUEST, 2600, "이미 존재하는 링크 url입니다."),
    INVALID_LINK_URL(HttpStatus.BAD_REQUEST, 2601, "유효하지 않은 url입니다."),
    LINK_SCRAPED_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 2602, "링크 url 스크랩 과정에서 에러가 발생했습니다."),
    LINK_NOT_FOUND(HttpStatus.NOT_FOUND, 2603, "링크를 찾을 수 없습니다."),
    LINK_ACCESS_DENIED(HttpStatus.FORBIDDEN, 2604, "해당 링크에 대한 접근이 거부되었습니다."),
    LINK_DELETE_DENIED(HttpStatus.BAD_REQUEST, 2605, "휴지통에 있는 링크만 영구삭제 할 수 있습니다."),
    ;

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    public String getMessage(Throwable e) {
        return this.getMessage(e.getMessage());
    }

    public String getMessage(String message) {

        return Optional.ofNullable(message)
                .filter(Predicate.not(String::isBlank))
                .orElse(this.getMessage());
    }

    public static Optional<ErrorCode> fromMessage(String message) {
        return Stream.of(values())
                .filter(errorCode -> errorCode.getMessage().equals(message))
                .findFirst();
    }

    @Override
    public String toString() {
        return String.format("%s %s", this.name(), this.getMessage());
    }

}
