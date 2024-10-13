package cmc.blink.domain.link.business;

import cmc.blink.domain.folder.business.FolderMapper;
import cmc.blink.domain.folder.implement.FolderCommandAdapter;
import cmc.blink.domain.folder.implement.FolderQueryAdapter;
import cmc.blink.domain.folder.persistence.Folder;
import cmc.blink.domain.link.implement.LinkCommandAdapter;
import cmc.blink.domain.link.implement.LinkFolderCommandAdapter;
import cmc.blink.domain.link.implement.LinkFolderQueryAdapter;
import cmc.blink.domain.link.implement.LinkQueryAdapter;
import cmc.blink.domain.link.persistence.Link;
import cmc.blink.domain.link.persistence.LinkFolder;
import cmc.blink.domain.link.presentation.dto.LinkRequest;
import cmc.blink.domain.link.presentation.dto.LinkResponse;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.exception.BadRequestException;
import cmc.blink.global.exception.FolderException;
import cmc.blink.global.exception.LinkException;
import cmc.blink.global.exception.constant.ErrorCode;
import cmc.blink.global.util.opengraph.OpenGraph;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkCommandAdapter linkCommandAdapter;
    private final LinkQueryAdapter linkQueryAdapter;
    private final FolderQueryAdapter folderQueryAdapter;
    private final FolderCommandAdapter folderCommandAdapter;
    private final LinkFolderQueryAdapter linkFolderQueryAdapter;
    private final LinkFolderCommandAdapter linkFolderCommandAdapter;
    private final LinkScrapper linkScrapper;

    @Value("${gcp.api-key}")
    private String apiKey;

    private static final List<String> USER_AGENT_LIST = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.1 Safari/605.1.15"
    );

    private static final Logger logger = LoggerFactory.getLogger(LinkService.class);

    @Transactional
    public LinkResponse.LinkCreateDto saveLink(LinkRequest.LinkCreateDto createDto, User user) throws Exception {
        String url = createDto.getUrl().trim().replaceAll("\\s+", "");

        try {
            if (linkQueryAdapter.isLinkUrlDuplicate(url, user)){
                if (linkQueryAdapter.findByUserAndUrl(user, url).isTrash())
                    throw new LinkException(ErrorCode.TRASH_LINK_URL);
                else
                    throw new LinkException(ErrorCode.DUPLICATE_LINK_URL);
            }

            // 입력받은 url 유효성 체크
            if (!isValidUrl(url))
                throw new LinkException(ErrorCode.INVALID_LINK_URL);

            String domain = extractDomain(url);

            LinkResponse.LinkInfo linkInfo = switch (domain) {
                case "youtu.be", "youtube.com" -> fetchYoutubeLinkInfo(url);
                case "instagram.com" -> fetchInstagramLinkInfo(url);
                case "blog.naver.com" -> fetchNaverBlogLinkInfo(url);
                case "cafe.naver.com" -> fetchNaverCafeLinkInfo(url);
                case "x.com" -> fetchTwitterLinkInfo(url);
                default -> fetchLinkInfo(url);
            };

            Link link = LinkMapper.toLink(url, user, linkInfo);
            link.validateAndSetFields(link.getTitle(), link.getContents(), link.getImageUrl());

            linkCommandAdapter.create(link);

            List<Folder> folders = createDto.getFolderIdList().stream()
                    .map(folderQueryAdapter::findById).toList();

            folders.stream()
                    .map(folderCommandAdapter::updateLastLinkedAt)
                    .forEach(folder -> linkFolderCommandAdapter.create(LinkFolderMapper.toLinkFolder(link, folder)));

            return LinkMapper.toLinkCreateDto(link);
        } catch (UnknownHostException e){
            throw new LinkException(ErrorCode.INVALID_LINK_URL);
        }
    }

    public void saveDefaultLink(User user, String language) {

        String defaultLink;
        String linkTitle;
        String contents;

        String folderTitle;

        if (language.equals("KO")){
            defaultLink = "https://yellow-harbor-c53.notion.site/B-Link-e3e97b00d5d045889a39b2bdf430805c?pvs=4";
            linkTitle = "\uD83D\uDC4B B.Link에 오신 것을 환영해요!";
            contents = "클릭해 B.Link를 더 알아보실래요?✨";

            folderTitle = "기본 폴더";
        } else if (language.equals("EN")) {
            defaultLink = "https://yellow-harbor-c53.notion.site/Welcome-to-B-Link-02556e48d0ce428e80f29e4f96c92855";
            linkTitle = "\uD83D\uDC4B Welcome to B.Link! ✨";
            contents = "Tap here to see what awaits you!";

            folderTitle = "Basic folder";
        } else {
            throw new BadRequestException(ErrorCode.INVALID_LANGUAGE);
        }

        Link link = linkCommandAdapter.create(LinkMapper.toLink(defaultLink, user, LinkMapper.toLinkInfo(linkTitle,
                "Default", contents, "")));

        Folder folder = folderCommandAdapter.create(FolderMapper.toFolder(folderTitle, user, 1));

        folderCommandAdapter.updateLastLinkedAt(folder);

        linkFolderCommandAdapter.create(LinkFolderMapper.toLinkFolder(link, folder));
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain != null ? domain.startsWith("www.") ? domain.substring(4) : domain : "";
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }
    }

    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            throw new LinkException(ErrorCode.INVALID_LINK_URL);
        }
    }

    private static String getRandomUserAgent() {
        Random random = new Random();
        return USER_AGENT_LIST.get(random.nextInt(USER_AGENT_LIST.size()));
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

    private LinkResponse.LinkInfo fetchYoutubeLinkInfo(String url) {
        try {
            // 유튜브 영상의 ID를 추출
            String videoId = extractVideoId(url);

            // YouTube Data API 클라이언트 생성
            YouTube youtube = new YouTube.Builder(
                    new com.google.api.client.http.javanet.NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    null
            ).setYouTubeRequestInitializer(new YouTubeRequestInitializer(apiKey))
                    .setApplicationName("youtube-data-api-example")
                    .build();

            // 동영상 정보 요청
            YouTube.Videos.List videoRequest = youtube.videos()
                    .list("snippet")
                    .setId(videoId);

            // API 응답
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
//            throw new LinkException(ErrorCode.LINK_SCRAPED_FAILED);
        }
    }

    private String extractVideoId(String url) throws LinkException {
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

    private String getOpenGraphContent(OpenGraph openGraph, String property) {
        return Optional.ofNullable(openGraph.getContent(property))
                .map(HtmlUtils::htmlUnescape)
                .orElse("");
    }

    private LinkResponse.LinkInfo fetchLinkInfoWithJsoup(String url) throws IOException {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(getRandomUserAgent())
                    .followRedirects(false)
                    .get();

            String title = doc.title();
            String type = doc.select("meta[name=type]").attr("content");
            String contents = doc.select("meta[name=description]").attr("content");
            String imageUrl = doc.select("meta[property=og:image]").attr("content");

            return LinkMapper.toLinkInfo(title, type, contents, imageUrl);
        } catch (UnsupportedMimeTypeException e) {
            return fetchLinkInfoWithBinary(url);
        }

    }

    private LinkResponse.LinkInfo fetchLinkInfoWithBinary(String url) throws IOException {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .execute();

            String htmlContent = new String(response.bodyAsBytes(), StandardCharsets.UTF_8);
            Document doc = Jsoup.parse(htmlContent);

            String title = doc.title();
            String contents = doc.select("meta[name=description]").attr("content");
            String imageUrl = doc.select("meta[property=og:image]").attr("content");

            return LinkMapper.toLinkInfo(title, "", contents, imageUrl);
        } catch (IOException e) {
            throw new LinkException(ErrorCode.LINK_SCRAPED_FAILED);
        }
    }

    @Transactional
    public LinkResponse.FolderIdListDto findLinkFolders(User user, Long linkId) {
        Link link = linkQueryAdapter.findById(linkId);

        if (link.getUser() != user)
            throw new LinkException(ErrorCode.LINK_ACCESS_DENIED);

        List<LinkFolder> linkFolders = linkFolderQueryAdapter.findAllByLink(link);

        return LinkMapper.toFolderIdListDto(linkFolders);
    }

    @Transactional
    public void updateTitle(LinkRequest.LinkTitleUpdateDto updateDto, Long linkId, User user) {
        Link link = linkQueryAdapter.findById(linkId);

        if (link.getUser() != user)
            throw new LinkException(ErrorCode.LINK_ACCESS_DENIED);

        linkCommandAdapter.updateTitle(link, updateDto);
    }

    @Transactional
    public void updateLastViewedAt(Long linkId, User user) {
        Link link = linkQueryAdapter.findById(linkId);

        if (link.getUser() != user)
            throw new LinkException(ErrorCode.LINK_ACCESS_DENIED);

        linkCommandAdapter.updateLastViewedAt(link);
        if (link.isExcluded())
            linkCommandAdapter.updateExcluded(link);
    }

    @Transactional
    public void updateExcluded(User user, Long linkId) {

        Link link = linkQueryAdapter.findById(linkId);

        if (link.getUser() != user)
            throw new LinkException(ErrorCode.LINK_ACCESS_DENIED);

        if (!link.isExcluded())
            linkCommandAdapter.updateExcluded(link);
        else
            throw new LinkException(ErrorCode.LINK_EXCLUDE_DENIED);
    }

    @Transactional
    public void moveLink(Long linkId, LinkRequest.LinkFolderMoveDto updateDto, User user) {
        Link link = linkQueryAdapter.findById(linkId);

        if (link.getUser() != user)
            throw new LinkException(ErrorCode.LINK_ACCESS_DENIED);

        List<LinkFolder> linkFolders = linkFolderQueryAdapter.findAllByLink(link);

        linkFolders.forEach(linkFolderCommandAdapter::delete);

        if (updateDto.getFolderIdList() != null) {
            updateDto.getFolderIdList().forEach(folderId -> {
                Folder folder = folderQueryAdapter.findById(folderId);
                LinkFolder linkFolder = LinkFolderMapper.toLinkFolder(link, folder);
                linkFolderCommandAdapter.create(linkFolder);
            });
        }
    }

    @Transactional
    public void moveLinkToTrash(Long linkId, User user) {
        Link link = linkQueryAdapter.findById(linkId);

        if (link.getUser() != user)
            throw new LinkException(ErrorCode.LINK_ACCESS_DENIED);

        linkCommandAdapter.moveToTrash(link);
    }

    @Transactional
    public void recoverLinkFromTrash(Long linkId, User user) {
        Link link = linkQueryAdapter.findById(linkId);

        if (link.getUser() != user)
            throw new LinkException(ErrorCode.LINK_ACCESS_DENIED);

        linkCommandAdapter.recoverFromTrash(link);
    }

    @Transactional
    public void deleteLink(Long linkId, User user) {
        Link link = linkQueryAdapter.findById(linkId);

        if (link.getUser() != user)
            throw new LinkException(ErrorCode.LINK_ACCESS_DENIED);

        if (!link.isTrash())
            throw new LinkException(ErrorCode.LINK_DELETE_DENIED);

        List<LinkFolder> linkFolders = linkFolderQueryAdapter.findAllByLink(link);

        linkFolders.forEach(linkFolderCommandAdapter::delete);

        linkCommandAdapter.delete(link);
    }

    @Transactional
    public void deleteExpiredLinks() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Link> expiredLinks = linkQueryAdapter.findLinksInTrashBefore(sevenDaysAgo);

        for (Link link : expiredLinks) {
            List<LinkFolder> linkFolders = linkFolderQueryAdapter.findAllByLink(link);
            linkFolders.forEach(linkFolderCommandAdapter::delete);
            linkCommandAdapter.delete(link);
        }
    }

    @Transactional
    public void toggleLink(Long linkId, User user) {
        Link link = linkQueryAdapter.findById(linkId);

        if (link.getUser() != user)
            throw new LinkException(ErrorCode.LINK_ACCESS_DENIED);

        linkCommandAdapter.toggleLink(link);
    }

    @Transactional
    public void toggleLink(LinkRequest.LinkToggleDto requestDto, User user) {
        Link link = linkQueryAdapter.findByUserAndUrl(user, requestDto.getUrl());

        linkCommandAdapter.toggleLink(link);
    }

    @Transactional(readOnly = true)
    public LinkResponse.LinkListDto searchLinks(String query, User user, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Link> linkPage = linkQueryAdapter.searchLinksByUserAndQuery(user, query, pageable);

        int linkCount = (int) linkPage.getTotalElements();

        List<LinkResponse.LinkDto> linkDtos = findRepresentFolderName(linkPage.getContent());

        return LinkMapper.toLinkListDto(linkDtos, linkCount);
    }

    @Transactional
    public LinkResponse.LinkListDto findLinkFolderPaging(User user, Long folderId, Pageable pageable) {
        Folder folder = folderQueryAdapter.findById(folderId);

        if (folder.getUser() != user) {
            throw new FolderException(ErrorCode.FOLDER_ACCESS_DENIED);
        }

        List<LinkFolder> linkFolders = linkFolderQueryAdapter.findAllByFolder(folder);
        List<Long> linkIds = linkFolders.stream().map(linkFolder -> linkFolder.getLink().getId()).collect(Collectors.toList());

        Page<Link> linksPage = linkQueryAdapter.findByIdsAndUserAndIsTrashFalse(linkIds, user, pageable);

        List<Link> links = linksPage.getContent();
        int linkCount = (int)linksPage.getTotalElements();

        List<LinkResponse.LinkDto> linkDtos = links.stream()
                .map(link -> LinkMapper.toLinkDto(link, folder.getTitle()))
                .collect(Collectors.toList());

        return LinkMapper.toLinkListDto(linkDtos, linkCount);

    }

    @Transactional
    public LinkResponse.LinkListDto findLinkPaging(User user, Pageable pageable) {

        Page<Link> linksPage = linkQueryAdapter.findByUserAndIsTrashFalse(user, pageable);

        List<Link> links = linksPage.getContent();
        int linkCount = linkQueryAdapter.countByUserAndIsTrashFalse(user);

        List<LinkResponse.LinkDto> linkDtos = findRepresentFolderName(links);

        return LinkMapper.toLinkListDto(linkDtos, linkCount);
    }

    public LinkResponse.LinkListDto findNoFolderLinkPaging(User user, Pageable pageable) {

        Page<Link> linksPage = linkQueryAdapter.findByUserAndNoFolderAndIsTrashFalse(user, pageable);

        List<Link> links = linksPage.getContent();
        int linkCount = linkQueryAdapter.countByUserAndNoFolderAndIsTrashFalse(user);

        List<LinkResponse.LinkDto> linkDtos = links.stream()
                .map(link -> LinkMapper.toLinkDto(link, "")).toList();

        return LinkMapper.toLinkListDto(linkDtos, linkCount);
    }

    @Transactional
    public LinkResponse.LinkListDto findPinnedLinks(User user, Pageable pageable) {

        Page<Link> linksPage = linkQueryAdapter.findPinnedLinksByUserAndIsTrashFalse(user, pageable);

        List<Link> links = linksPage.getContent();
        int linkCount = linkQueryAdapter.countPinnedLinksByUserAndIsTrashFalse(user);

        List<LinkResponse.LinkDto> linkDtos = findRepresentFolderName(links);

        return LinkMapper.toLinkListDto(linkDtos, linkCount);
    }

    @Transactional
    public LinkResponse.LinkListDto findTrashLinks(User user, Pageable pageable) {
        Page<Link> linksPage = linkQueryAdapter.findTrashLinksByUser(user, pageable);

        List<Link> links = linksPage.getContent();
        int linkCount = linkQueryAdapter.countTrashLinksByUser(user);

        List<LinkResponse.LinkDto> linkDtos = findRepresentFolderName(links);

        return LinkMapper.toLinkListDto(linkDtos, linkCount);
    }

    @Transactional
    public LinkResponse.LastViewedLinkListDto findLastViewedLinks(User user) {

        List<Link> links = linkQueryAdapter.findTop5LastViewedLinksByUser(user);

        List<LinkResponse.LinkDto> linkDtos = findRepresentFolderName(links);

        return LinkMapper.toLastViewedLinkListDto(linkDtos);
    }

    private List<LinkResponse.LinkDto> findRepresentFolderName(List<Link> links) {
        Map<Long, List<String>> folderNamesMap = linkFolderQueryAdapter.findFolderTitlesForLinks(links);

        List<LinkResponse.LinkDto> linkDtos = links.stream()
                .map(link -> {
                    List<String> folderNames = folderNamesMap.getOrDefault(link.getId(), null);
                    String folderName = null;

                    if (folderNames != null && !folderNames.isEmpty()) {
                        folderName = folderNames.get(0);
                        if (folderNames.size() > 1) {
                            folderName += ", ...";
                        }
                    }

                    return LinkMapper.toLinkDto(link, folderName);
                }).toList();

        return linkDtos;
    }

    @Transactional
    public Boolean checkLinkExists(LinkRequest.LinkToggleDto requestDto, User user) {
        return linkQueryAdapter.isLinkUrlDuplicate(requestDto.getUrl(), user);
    }
}
