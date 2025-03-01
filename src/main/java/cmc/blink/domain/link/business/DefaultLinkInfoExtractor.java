package cmc.blink.domain.link.business;

import cmc.blink.domain.link.presentation.dto.LinkResponse;
import cmc.blink.global.exception.LinkException;
import cmc.blink.global.exception.constant.ErrorCode;
import cmc.blink.global.util.opengraph.OpenGraph;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
public class DefaultLinkInfoExtractor implements LinkInfoExtractor {

    private static final List<String> USER_AGENT_LIST = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.1 Safari/605.1.15"
    );

    @Value("${gcp.api-key}")
    private String apiKey;

    @Override
    public LinkResponse.LinkInfo extractInfo(String domain, String url) throws Exception {
        return switch (domain) {
            case "youtu.be", "youtube.com" -> fetchYoutubeLinkInfo(url);
            case "instagram.com" -> fetchInstagramLinkInfo(url);
            case "blog.naver.com" -> fetchNaverBlogLinkInfo(url);
            case "cafe.naver.com" -> fetchNaverCafeLinkInfo(url);
            case "x.com" -> fetchTwitterLinkInfo(url);
            case "brunch.co.kr" -> fetchBrunchLinkInfo(url);
            case "reddit.com" -> fetchRedditLinkInfo(url);
            default -> fetchLinkInfo(url);
        };
    }

    private LinkResponse.LinkInfo fetchYoutubeLinkInfo(String url) {
        try {
            String videoId = extractYoutubeVideoId(url);

            YouTube youtube = new YouTube.Builder(
                    new com.google.api.client.http.javanet.NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    null
            ).setYouTubeRequestInitializer(new YouTubeRequestInitializer(apiKey))
                    .setApplicationName("youtube-data-api-example")
                    .build();

            YouTube.Videos.List videoRequest = youtube.videos()
                    .list("snippet")
                    .setId(videoId);

            VideoListResponse response = videoRequest.execute();
            List<Video> videoList = response.getItems();

            if (videoList.isEmpty()) {
                throw new LinkException(ErrorCode.INVALID_LINK_URL);
            }

            Video video = videoList.get(0);

            String title = video.getSnippet().getTitle();

            String channelTitle = video.getSnippet().getChannelTitle();

            String description = video.getSnippet().getDescription();

            String thumbnailUrl = video.getSnippet().getThumbnails().getDefault().getUrl();

            String type = "Youtube";

            String contents = String.format("%s | %s", channelTitle, description);

            return LinkMapper.toLinkInfo(title, type, contents, thumbnailUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractYoutubeVideoId(String url) throws LinkException {
        try {
            if (url.contains("youtu.be/")) {
                return url.substring(url.lastIndexOf("/") + 1).split("\\?")[0];
            }

            if (url.contains("youtube.com/shorts/")) {
                return url.substring(url.indexOf("/shorts/") + 8).split("\\?")[0];
            }

            URL videoUrl = new URL(url);
            String query = videoUrl.getQuery();

            if (query != null) {
                String[] queryParams = query.split("&");
                for (String param : queryParams) {
                    if (param.startsWith("v=")) {
                        return param.split("=")[1];
                    }
                }
            }

            String path = videoUrl.getPath();
            if (path.contains("/embed/")) {
                return path.substring(path.lastIndexOf("/embed/") + 7);
            }

        } catch (Exception e) {
            throw new LinkException(ErrorCode.INVALID_LINK_URL);
        }
        throw new LinkException(ErrorCode.INVALID_LINK_URL);
    }

    private LinkResponse.LinkInfo fetchInstagramLinkInfo(String url) {
        try {
            OpenGraph openGraph = new OpenGraph(url, true);

            String type = getOpenGraphContent(openGraph, "site_name");

            if (type.isEmpty()) {
                type = "Instagram Profile";
            }

            String title = getOpenGraphContent(openGraph, "title");
            int titleIndex = title.indexOf("on Instagram: ");
            if (titleIndex != -1 && title.length() > titleIndex + "on Instagram: ".length()) {
                title = title.substring(titleIndex + "on Instagram: ".length()).trim();
            } else if (type.equals("Instagram Profile")) {
                title = title.trim();
            } else {
                title = "";
            }

            String contents = getOpenGraphContent(openGraph, "description");
            int contentIndex = contents.indexOf(": ");
            if (contentIndex != -1 && contents.length() > contentIndex + 2) {
                contents = contents.substring(contentIndex + 2).trim();
            } else if (type.equals("Instagram Profile")) {
                contents = contents.trim();
            } else {
                contents = "";
            }

            String imageUrl = getOpenGraphContent(openGraph, "image");

            return LinkMapper.toLinkInfo(title, type, contents, imageUrl);
        } catch (Exception e) {
            throw new LinkException(ErrorCode.LINK_SCRAPED_FAILED);
        }
    }

    private LinkResponse.LinkInfo fetchNaverBlogLinkInfo(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            Element iframe = doc.selectFirst("iframe#mainFrame");
            if (iframe == null) {
                throw new LinkException(ErrorCode.LINK_SCRAPED_FAILED);
            }
            String postUrl = "https://blog.naver.com" + iframe.attr("src");

            Document postDoc = Jsoup.connect(postUrl).get();

            String title = postDoc.title();

            String contents = postDoc.select(".se-main-container").text();
            if (contents.length() > 300) {
                contents = contents.substring(0, 300);
            }
            Elements images = postDoc.select(".se-main-container img");
            String imageUrl = "";
            for (Element img : images) {
                imageUrl = img.attr("src");
                break;
            }

            return LinkMapper.toLinkInfo(title, "Naver", contents, imageUrl);
        } catch (IOException e) {
            throw new LinkException(ErrorCode.LINK_SCRAPED_FAILED);
        }
    }

    private LinkResponse.LinkInfo fetchNaverCafeLinkInfo(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            String title = doc.select("meta[property=og:title]").attr("content");
            if (title.isEmpty()) {
                title = doc.title();
            }

            String contents = doc.select("meta[property=og:description]").attr("content");
            if (contents.isEmpty()) {
                contents = doc.select("meta[name=description]").attr("content"); // Another fallback
            }

            String imageUrl = doc.select("meta[property=og:image]").attr("content");

            return LinkMapper.toLinkInfo(title, "Naver", contents, imageUrl);
        } catch (IOException e) {
            throw new LinkException(ErrorCode.LINK_SCRAPED_FAILED);
        }
    }

    private LinkResponse.LinkInfo fetchTwitterLinkInfo(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .followRedirects(true)
                    .get();

            String title = doc.select("meta[property=og:title]").attr("content");
            if (title.isEmpty()) {
                title = doc.title();  // Fallback to the regular title if og:title is not present
            }

            String type = doc.select("meta[property=og:site_name]").attr("content");
            if (type.isEmpty()) {
                type = "Twitter";
            }

            String contents = doc.select("meta[property=og:description]").attr("content");
            if (contents.isEmpty()) {
                contents = doc.select("meta[name=description]").attr("content"); // Another fallback
            }

            String imageUrl = doc.select("meta[property=og:image]").attr("content");

            return LinkMapper.toLinkInfo(title, type, contents, imageUrl);
        } catch (IOException e) {
            throw new LinkException(ErrorCode.LINK_SCRAPED_FAILED);
        }
    }

    private LinkResponse.LinkInfo fetchBrunchLinkInfo(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            System.out.printf("<<<<brunch link scrapping result HTML>>>>\n\n\n %s\n\n\n%n", doc);

            String title = doc.select("meta[property=og:title]").attr("content");

            if (title.isEmpty()) {
                title = doc.title();
            }

            String contents = doc.select("meta[property=og:description]").attr("content");
            if (contents.isEmpty()) {
                contents = doc.select("meta[name=description]").attr("content");
            }

            String imageUrl = doc.select("meta[property=og:image]").attr("content");

            return LinkMapper.toLinkInfo(title, "Brunch", contents, imageUrl);

        } catch (IOException e) {
            throw new LinkException(ErrorCode.LINK_SCRAPED_FAILED);
        }
    }

    private LinkResponse.LinkInfo fetchRedditLinkInfo(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(getRandomUserAgent())
                    .ignoreContentType(true)
                    .get();

            Element titleElement = doc.selectFirst("shreddit-title");
            String fullTitle = titleElement != null ? titleElement.attr("title") : "Title not found";
            String title = fullTitle.contains(":") ? fullTitle.split(":", 2)[0].trim() : fullTitle;

            Element subredditElement = doc.selectFirst("shreddit-post");
            String contents = subredditElement != null ? subredditElement.attr("subreddit-prefixed-name") : "Subreddit not found";

            Element videoElement = doc.selectFirst("shreddit-post");
            String imageUrl = videoElement != null ? videoElement.attr("content-href") : "";

            return LinkMapper.toLinkInfo(title, "Reddit", contents, imageUrl);
        } catch (Exception e) {
            throw new LinkException(ErrorCode.LINK_SCRAPED_FAILED);
        }
    }

    private String getOpenGraphContent(OpenGraph openGraph, String property) {
        return Optional.ofNullable(openGraph.getContent(property))
                .map(HtmlUtils::htmlUnescape)
                .orElse("");
    }


    private LinkResponse.LinkInfo fetchLinkInfo(String url) throws Exception {
        try {
            OpenGraph openGraph = new OpenGraph(url, true);

            if (openGraph.getProperties().length==0)
                return fetchLinkInfoWithJsoup(url);

            String title = getOpenGraphContent(openGraph, "title");
            String type = getOpenGraphContent(openGraph, "site_name");
            String contents = getOpenGraphContent(openGraph, "description");
            String imageUrl = getOpenGraphContent(openGraph, "image");

            return LinkMapper.toLinkInfo(title, type, contents, imageUrl);
        } catch (UnknownHostException e) {
            throw new LinkException(ErrorCode.INVALID_LINK_URL);
        } catch (ProtocolException e) {
            return fetchLinkInfoWithJsoup(url);
        }
    }

    private LinkResponse.LinkInfo fetchLinkInfoWithJsoup(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .userAgent(getRandomUserAgent())
                .ignoreContentType(true)
                .followRedirects(true)
                .get();

        String title = doc.title();
        String type = doc.select("meta[name=type]").attr("content");
        String contents = doc.select("meta[name=description]").attr("content");
        String imageUrl = doc.select("meta[property=og:image]").attr("content");

        return LinkMapper.toLinkInfo(title, type, contents, imageUrl);
    }

    private static String getRandomUserAgent() {
        Random random = new Random();
        return USER_AGENT_LIST.get(random.nextInt(USER_AGENT_LIST.size()));
    }
}
