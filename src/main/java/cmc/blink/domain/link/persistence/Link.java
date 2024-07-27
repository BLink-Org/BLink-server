package cmc.blink.domain.link.persistence;

import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
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

    @Column
    private boolean isExcluded;

    @Column
    private boolean isTrash;

    @Column
    private LocalDate trashMovedDate;

}
