package cmc.blink.global.scheduler;

import cmc.blink.domain.link.business.LinkService;
import cmc.blink.domain.user.business.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduledTasks {

    private final UserService userService;
    private final LinkService linkService;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteExpiredLinks() {
        linkService.deleteExpiredLinks();
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteExpiredAccounts() {
        userService.deleteExpiredAccounts();
    }

}
