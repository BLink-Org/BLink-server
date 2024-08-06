package cmc.blink.global.security.client;

import com.fasterxml.jackson.databind.JsonNode;
import feign.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "googleOauth2Client", url = "https://oauth2.googleapis.com", configuration = GoogleFeignClientConfig.class)
public interface GoogleOauth2Client {

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers("Content-Type: application/x-www-form-urlencoded")
    JsonNode getAccessToken(
            @RequestParam("code") String code,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("grant_type") String grantType);

}
