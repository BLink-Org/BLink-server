package cmc.blink.domain.link.implement;

import cmc.blink.domain.link.business.LinkMapper;
import cmc.blink.domain.link.persistence.Link;
import cmc.blink.domain.link.persistence.LinkRepository;
import cmc.blink.domain.link.presentation.dto.LinkResponse;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.annotation.Adapter;
import cmc.blink.global.exception.LinkException;
import cmc.blink.global.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Adapter
@RequiredArgsConstructor
public class LinkQueryAdapter {

    private final LinkRepository linkRepository;



    public boolean isLinkUrlDuplicate(String url, User user){
        return linkRepository.existsByUrlAndUser(url, user);
    }

    public Link findById(Long id) {
        return linkRepository.findById(id).orElseThrow(()-> new LinkException(ErrorCode.LINK_NOT_FOUND));
    }

    public Link findByUserAndUrl(User user, String url) {
        return linkRepository.findByUserAndUrl(user, url).orElseThrow(()-> new LinkException(ErrorCode.LINK_NOT_FOUND));
    }

    public int countByUser(User user) {
        return linkRepository.countByUser(user);
    }

    public int countByUserAndNoFolderAndIsTrashFalse(User user){
        return linkRepository.countByUserAndNoFolderAndIsTrashFalse(user);
    }

    public Page<Link> findByIdsAndUserAndIsTrashFalse(List<Long> linkIds, User user, Pageable pageable) {
        return linkRepository.findByIdInAndUserAndIsTrashFalse(linkIds, user, pageable);
    }

    public Page<Link> findByUserAndIsTrashFalse(User user, Pageable pageable) {
        return linkRepository.findByUserAndIsTrashFalse(user, pageable);
    }

    public Page<Link> findByUserAndNoFolderAndIsTrashFalse(User user, Pageable pageable) {
        return linkRepository.findByUserAndNoFolderAndIsTrashFalse(user, pageable);
    }

    public int countByUserAndIsTrashFalse(User user) {
        return linkRepository.countByUserAndIsTrashFalse(user);
    }

    public Page<Link> findPinnedLinksByUserAndIsTrashFalse(User user, Pageable pageable) {
        return linkRepository.findByUserAndIsPinnedTrueAndIsTrashFalse(user, pageable);
    }

    public int countPinnedLinksByUserAndIsTrashFalse(User user) {
        return linkRepository.countByUserAndIsPinnedTrueAndIsTrashFalse(user);
    }

    public Page<Link> findTrashLinksByUser(User user, Pageable pageable) {
        return linkRepository.findByUserAndIsTrashTrue(user, pageable);
    }

    public int countTrashLinksByUser(User user) {
        return linkRepository.countByUserAndIsTrashTrue(user);
    }

    public List<Link> findTop5LastViewedLinksByUser(User user) {
        Pageable pageable = PageRequest.of(0,5);
        return linkRepository.findTop5LastViewedLinksByUser(user, LocalDateTime.now().minusDays(31), pageable);
    }

    public Page<Link> searchLinksByUserAndQuery(User user, String query, Pageable pageable) {
        return linkRepository.searchLinksByUserAndQuery(user, query, pageable);
    }
}
