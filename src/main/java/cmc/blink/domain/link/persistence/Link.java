package cmc.blink.domain.link.persistence;

import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@DynamicInsert
@DynamicUpdate
@Getter
@Builder
public class Link extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, columnDefinition = "VARCHAR(2000)")
    private String url;

    @Column(nullable = false, columnDefinition = "VARCHAR(400)")
    private String title;

    @Column
    private String type;

    @Column(columnDefinition = "VARCHAR(2000)")
    private String contents;

    @Column(nullable = false, columnDefinition = "VARCHAR(2000)")
    private String imageUrl;

    @Column
    private LocalDateTime lastViewedAt;

    @ColumnDefault("false")
    private boolean isExcluded;

    @ColumnDefault("false")
    private boolean isTrash;

    @Column
    private LocalDateTime trashMovedDate;

    @ColumnDefault("false")
    private boolean isPinned;

    @Column
    private LocalDateTime pinnedAt;

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateLastViewedAt() {
        this.lastViewedAt = LocalDateTime.now();
    }

    public void moveToTrash() {
        if (!this.isTrash){
            this.isTrash = true;
            this.trashMovedDate = LocalDateTime.now();
        }
    }

    public void recoverFromTrash() {
        if (this.isTrash) {
            this.isTrash = false;
            this.trashMovedDate = null;
        }
    }

    public boolean togglePin() {
        if (this.isPinned) {
            this.isPinned = false;
            this.pinnedAt = null;
        } else {
            this.isPinned = true;
            this.pinnedAt = LocalDateTime.now();
        }

        return this.isPinned;
    }


}
