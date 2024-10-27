package cmc.blink.domain.link.business;

import cmc.blink.domain.link.implement.LinkQueryAdapter;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.exception.LinkException;
import cmc.blink.global.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Component
@RequiredArgsConstructor
public class LinkValidator {

    private final LinkQueryAdapter linkQueryAdapter;

    public void validate(String url, User user) {
        if (url == null || url.isEmpty()) {
            throw new LinkException(ErrorCode.INVALID_LINK_URL);
        }

        if (!isValidUrlFormat(url)) {
            throw new LinkException(ErrorCode.INVALID_LINK_URL);
        }

        checkUrlAccessibility(url);

        if (isLinkUrlDuplicate(url, user)) {
            if (isLinkInTrash(url, user)) {
                throw new LinkException(ErrorCode.TRASH_LINK_URL);
            } else {
                throw new LinkException(ErrorCode.DUPLICATE_LINK_URL);
            }
        }
    }

    private boolean isValidUrlFormat(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void checkUrlAccessibility(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();

            if (responseCode >= 400) {
                throw new LinkException(ErrorCode.INVALID_LINK_URL);
            }
        } catch (IOException e) {
            throw new LinkException(ErrorCode.INVALID_LINK_URL);
        }
    }

    public String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain != null && domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            throw new LinkException(ErrorCode.INVALID_LINK_URL);
        }
    }

    private boolean isLinkUrlDuplicate(String url, User user) {
        return linkQueryAdapter.isLinkUrlDuplicate(url, user);
    }

    private boolean isLinkInTrash(String url, User user) {
        return linkQueryAdapter.findByUserAndUrl(user, url).isTrash();
    }
}
