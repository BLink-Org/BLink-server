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
import cmc.blink.global.exception.LinkException;
import cmc.blink.global.exception.constant.ErrorCode;
import cmc.blink.global.util.opengraph.OpenGraph;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
    public LinkResponse.LinkCreateDto saveLink(LinkRequest.LinkCreateDto createDto, User user) {
        // 입력받은 url이 사용자가 이미 저장했던 링크인지 검증
        if (linkQueryAdapter.isLinkUrlDuplicate(createDto.getUrl(), user))
            throw new LinkException(ErrorCode.DUPLICATE_LINK_URL);

        // 입력받은 url 유효성 체크
        if (!isValidUrl(createDto.getUrl()))
            throw new LinkException(ErrorCode.INVALID_LINK_URL);

        // 입력받은 url 웹 스크래핑 하여 링크 테이블에 저장할 필드(제목, 타입, 본문, 이미지 url) 가져오기
        LinkResponse.LinkInfo linkInfo = null;
        try {
            linkInfo = fetchLinkInfo(createDto.getUrl());
        } catch (IOException e) {
            throw new LinkException(ErrorCode.LINK_SCRAPED_FAILED);
        }

        // 링크 레코드 생성
        Link link = linkCommandAdapter.create(LinkMapper.toLink(createDto.getUrl(), user, linkInfo));

        List<Folder> folders = folderQueryAdapter.findAllById(createDto.getFolderIdList());

        folders.stream()
                .map(folderCommandAdapter::updateLastLinkedAt)
                .forEach(folder -> linkFolderCommandAdapter.create(LinkFolderMapper.toLinkFolder(link, folder)));

        return LinkMapper.toLinkCreateDto(link);
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
    public void updateTitle(LinkRequest.LinkTitleUpdateDto updateDto, Long linkId, User user) {
        Link link = linkQueryAdapter.findById(linkId);

        if (link.getUser() != user)
            throw new LinkException(ErrorCode.LINK_ACCESS_DENIED);

        linkCommandAdapter.updateTitle(link, updateDto);
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
}
