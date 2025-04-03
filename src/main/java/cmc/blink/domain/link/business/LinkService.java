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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final LinkValidator linkValidator;
    private final LinkInfoExtractor linkInfoExtractor;

    private static final Logger logger = LoggerFactory.getLogger(LinkService.class);

    @Transactional
    public LinkResponse.LinkCreateDto saveLink(LinkRequest.LinkCreateDto createDto, User user) throws Exception {
        String url = createDto.getUrl().trim().replaceAll("\\s+", "");

        linkValidator.validate(url, user);

        String domain = linkValidator.extractHost(url);
        LinkResponse.LinkInfo linkInfo = linkInfoExtractor.extractInfo(domain, url);

        Link link = LinkMapper.toLink(url, user, linkInfo);
        link.validateAndSetFields(link.getTitle(), link.getContents(), link.getImageUrl());
        linkCommandAdapter.create(link);

        List<Folder> folders = createDto.getFolderIdList().stream()
                .map(folderQueryAdapter::findById).toList();

        folders.stream()
                .map(folderCommandAdapter::updateLastLinkedAt)
                .forEach(folder -> linkFolderCommandAdapter.create(LinkFolderMapper.toLinkFolder(link, folder)));

        return LinkMapper.toLinkCreateDto(link);
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
