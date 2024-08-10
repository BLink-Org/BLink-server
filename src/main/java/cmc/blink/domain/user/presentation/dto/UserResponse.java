package cmc.blink.domain.user.presentation.dto;

import lombok.Builder;
import lombok.Getter;

public class UserResponse {

    @Getter
    @Builder
    public static class UserInfo {
        String email;
        boolean deleteRequest;
        int linkCount;
        int pinCount;
        int folderCount;
    }
}
