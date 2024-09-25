package cmc.blink.domain.user.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

public class UserResponse {

    @Getter
    @Builder
    public static class UserInfo {
        String email;
        boolean deleteRequest;
        LocalDate deleteRequestDate;
        int linkCount;
        int pinCount;
        int folderCount;
    }
}
