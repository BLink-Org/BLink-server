package cmc.blink.global.security;

import cmc.blink.global.exception.OAuth2Exception;
import cmc.blink.global.exception.constant.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class OAuth2UserAttributes {

    private String name;
    private String email;
    private String provider;

    static OAuth2UserAttributes of(String provider, String attributeKey, Map<String, Object> attributes) {
        switch (provider) {
            case "google":
                return ofGoogle(provider, attributeKey, attributes);
//            case "apple":
//                return ofApple(provider, attributeKey, attributes);
            default:
                throw new OAuth2Exception(ErrorCode.INVALID_OAUTH2_PROVIDER);
        }
    }

//    private static OAuth2UserAttributes ofApple(String provider, String attributeKey, Map<String, Object> attributes) {
//        Map<String, String> nameAttributes = (Map<String, String>) attributes.get("name");
//
//        String name = "";
//        if (nameAttributes != null) {
//            String firstName = nameAttributes.getOrDefault("firstName", "");
//            String lastName = nameAttributes.getOrDefault("lastName", "");
//            name = firstName + lastName;
//        }
//
//        String email = (String) attributes.getOrDefault("email", "");
//
//        return OAuth2UserAttributes.builder()
//                .name(name)
//                .email(email)
//                .provider(provider)
//                .build();
//    }

    private static OAuth2UserAttributes ofGoogle(String provider, String attributeKey, Map<String, Object> attributes) {
        return OAuth2UserAttributes.builder()
                .name((String)attributes.get("name"))
                .email((String)attributes.get("email"))
                .provider(provider)
                .build();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("provider", provider);
        return map;
    }

}
