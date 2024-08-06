package cmc.blink.global.security.client;

import cmc.blink.global.security.dto.GoogleUserInfo;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "googleUserFeignClient", url = "https://www.googleapis.com/oauth2/v3", configuration = GoogleFeignClientConfig.class)
public interface GoogleUserFeignClient {

    @GetMapping("/userinfo")
    GoogleUserInfo getUserInfo(@RequestHeader("Authorization") String authorization);
}
