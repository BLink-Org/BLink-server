package cmc.blink.global.security.dto;

import lombok.Getter;

@Getter
public class ApplePublicKey {
    String kty;
    String kid;
    String alg;
    String use;
    String n;
    String e;
}
