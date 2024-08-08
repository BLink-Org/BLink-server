package cmc.blink.global.security.client;

import cmc.blink.global.security.dto.GoogleUserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "googleTokenVerifierClient", url = "https://oauth2.googleapis.com")
public interface GoogleTokenVerifierClient {

    @GetMapping("/tokeninfo")
    GoogleUserInfo verifyIdToken(@RequestParam("id_token") String idToken);

}
