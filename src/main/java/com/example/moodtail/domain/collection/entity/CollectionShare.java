package com.example.moodtail.domain.collection.entity;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.global.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "collection_shares",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_collection_share_user",
                        columnNames = "user_id"
                ),
                @UniqueConstraint(
                        name = "uk_collection_share_token",
                        columnNames = "share_token"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionShare extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자당 공유 데이터는 최대 1개
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(
            name = "share_token",
            nullable = false,
            unique = true,
            length = 64
    )
    private String shareToken;

    @Column(
            name = "thumbnail_image_url",
            nullable = false,
            length = 2048
    )
    private String thumbnailImageUrl;


    @Column(nullable = false)
    private long version;

    private CollectionShare(
            User user,
            String shareToken,
            String thumbnailImageUrl
    ) {
        this.user = user;
        this.shareToken = shareToken;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.version = 1L;
    }

    public static CollectionShare create(
            User user,
            String shareToken,
            String thumbnailImageUrl
    ) {
        return new CollectionShare(
                user,
                shareToken,
                thumbnailImageUrl
        );
    }

    public void updateThumbnailImageUrl(String thumbnailImageUrl) {
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.version++;
    }
}
