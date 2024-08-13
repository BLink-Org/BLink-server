package cmc.blink.domain.link.business;

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
import cmc.blink.global.exception.FolderException;
import cmc.blink.global.exception.LinkException;
import cmc.blink.global.exception.constant.ErrorCode;
import cmc.blink.global.util.opengraph.OpenGraph;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Transactional
    public LinkResponse.LinkCreateDto saveLink(LinkRequest.LinkCreateDto createDto, User user) throws Exception {
        // 입력받은 url이 사용자가 이미 저장했던 링크인지 검증
        if (linkQueryAdapter.isLinkUrlDuplicate(createDto.getUrl(), user))
            throw new LinkException(ErrorCode.DUPLICATE_LINK_URL);

        // 입력받은 url 유효성 체크
        if (!isValidUrl(createDto.getUrl()))
            throw new LinkException(ErrorCode.INVALID_LINK_URL);

        // Extract domain and fetch link info based on domain
        String domain = extractDomain(createDto.getUrl());
        LinkResponse.LinkInfo linkInfo;

        switch (domain) {
            case "youtu.be":
            case "youtube.com":
                linkInfo = fetchYoutubeLinkInfo(createDto.getUrl());
                break;
            case "instagram.com":
                linkInfo = fetchInstagramLinkInfo(createDto.getUrl());
                break;
            case "blog.naver.com":
            case "cafe.naver.com":
                linkInfo = fetchNaverLinkInfo(createDto.getUrl());
                break;
            case "twitter.com":
                linkInfo = fetchTwitterLinkInfo(createDto.getUrl());
                break;

            default:
                linkInfo = fetchLinkInfo(createDto.getUrl());
                break;
        }

        // 링크 레코드 생성
        Link link = linkCommandAdapter.create(LinkMapper.toLink(createDto.getUrl(), user, linkInfo));

        List<Folder> folders = folderQueryAdapter.findAllById(createDto.getFolderIdList());

        folders.stream()
                .map(folderCommandAdapter::updateLastLinkedAt)
                .forEach(folder -> linkFolderCommandAdapter.create(LinkFolderMapper.toLinkFolder(link, folder)));

        return LinkMapper.toLinkCreateDto(link);
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
            return false;
        }
    }

    private LinkResponse.LinkInfo fetchLinkInfo(String url) throws IOException {
        try {
            OpenGraph openGraph = new OpenGraph(url, true);

            String title = getOpenGraphContent(openGraph, "title");
            String type = openGraph.getBaseType();
            String contents = getOpenGraphContent(openGraph, "description");
            String imageUrl = getOpenGraphContent(openGraph, "image");

            return LinkMapper.toLinkInfo(title, type, contents, imageUrl);

        } catch (Exception e) {
            return fetchLinkInfoWithJsoup(url);}
    }

    private LinkResponse.LinkInfo fetchYoutubeLinkInfo(String url) throws Exception {
        OpenGraph openGraph = new OpenGraph(url, true);
        Document doc = Jsoup.connect(url).get();

        String title = getOpenGraphContent(openGraph, "title");
        String type = getOpenGraphContent(openGraph, "site_name");
        String channelTitle = doc.select("meta[itemprop='author']").attr("content");
        if (channelTitle.isEmpty()) {
            channelTitle = doc.select("link[itemprop='name']").attr("content");
        }
        String contents = getOpenGraphContent(openGraph, "description");

        contents = String.format("%s | %s",channelTitle, contents);

        String imageUrl = getOpenGraphContent(openGraph, "image");

        return LinkMapper.toLinkInfo(title, type, contents, imageUrl);
    }

    private LinkResponse.LinkInfo fetchInstagramLinkInfo(String url) throws Exception {
        OpenGraph openGraph = new OpenGraph(url, true);

        System.out.println("openGraph = " + openGraph);

        String title = getOpenGraphContent(openGraph, "title");
        String type = openGraph.getBaseType();
        String contents = getOpenGraphContent(openGraph, "description");
        String imageUrl = getOpenGraphContent(openGraph, "image");

        return LinkMapper.toLinkInfo(title, type, contents, imageUrl);

    }

    private LinkResponse.LinkInfo fetchNaverLinkInfo(String url) throws Exception {
        OpenGraph openGraph = new OpenGraph(url, true);

        System.out.println("openGraph = " + openGraph);

        String title = getOpenGraphContent(openGraph, "title");
        String type = openGraph.getBaseType();
        String contents = getOpenGraphContent(openGraph, "description");
        String imageUrl = getOpenGraphContent(openGraph, "image");

        return LinkMapper.toLinkInfo(title, type, contents, imageUrl);
    }


    private LinkResponse.LinkInfo fetchTwitterLinkInfo(String url) throws Exception {
        OpenGraph openGraph = new OpenGraph(url, true);

        System.out.println("openGraph = " + openGraph);

        String title = getOpenGraphContent(openGraph, "title");
        String type = openGraph.getBaseType();
        String contents = getOpenGraphContent(openGraph, "description");
        String imageUrl = getOpenGraphContent(openGraph, "image");

        return LinkMapper.toLinkInfo(title, type, contents, imageUrl);
    }

    private String getOpenGraphContent(OpenGraph openGraph, String property) {
        return Optional.ofNullable(openGraph.getContent(property))
                .map(HtmlUtils::htmlUnescape)
                .orElse("");
    }

    private LinkResponse.LinkInfo fetchLinkInfoWithJsoup(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

        String title = doc.title();
        String type = doc.select("meta[name=type]").attr("content");
        String contents = doc.select("meta[name=description]").attr("content");
        String imageUrl = doc.select("meta[property=og:image]").attr("content");

        return LinkMapper.toLinkInfo(title, type, contents, imageUrl);
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
    public void toggleLink(Long linkId, User user) {
        Link link = linkQueryAdapter.findById(linkId);

        if (link.getUser() != user)
            throw new LinkException(ErrorCode.LINK_ACCESS_DENIED);

        linkCommandAdapter.toggleLink(link);
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
        int linkCount = linkFolders.size();

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

        Map<Long, List<String>> folderNamesMap = linkFolderQueryAdapter.findFolderTitlesForLinks(links);

        List<LinkResponse.LinkDto> linkDtos = links.stream()
                .map(link -> {
                    List<String> folderNames = folderNamesMap.getOrDefault(link.getId(), null);
                    String folderName = null;

                    if (folderNames != null && !folderNames.isEmpty()) {
                        folderName = folderNames.get(0);
                        if (folderNames.size() > 1) {
                            folderName += " 외";
                        }
                    }

                    return LinkMapper.toLinkDto(link, folderName);
                }).toList();

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

        Map<Long, List<String>> folderNamesMap = linkFolderQueryAdapter.findFolderTitlesForLinks(links);

        List<LinkResponse.LinkDto> linkDtos = links.stream()
                .map(link -> {
                    List<String> folderNames = folderNamesMap.getOrDefault(link.getId(), null);
                    String folderName = null;

                    if (folderNames != null && !folderNames.isEmpty()) {
                        folderName = folderNames.get(0);
                        if (folderNames.size() > 1) {
                            folderName += " 외";
                        }
                    }

                    return LinkMapper.toLinkDto(link, folderName);
                }).toList();

        return LinkMapper.toLinkListDto(linkDtos, linkCount);
    }

    @Transactional
    public LinkResponse.LinkListDto findTrashLinks(User user, Pageable pageable) {
        Page<Link> linksPage = linkQueryAdapter.findTrashLinksByUser(user, pageable);

        List<Link> links = linksPage.getContent();
        int linkCount = linkQueryAdapter.countTrashLinksByUser(user);

        Map<Long, List<String>> folderNamesMap = linkFolderQueryAdapter.findFolderTitlesForLinks(links);

        List<LinkResponse.LinkDto> linkDtos = links.stream()
                .map(link -> {
                    List<String> folderNames = folderNamesMap.getOrDefault(link.getId(), null);
                    String folderName = null;

                    if (folderNames != null && !folderNames.isEmpty()) {
                        folderName = folderNames.get(0);
                        if (folderNames.size() > 1) {
                            folderName += " 외";
                        }
                    }

                    return LinkMapper.toLinkDto(link, folderName);
                }).toList();

        return LinkMapper.toLinkListDto(linkDtos, linkCount);
    }
}
