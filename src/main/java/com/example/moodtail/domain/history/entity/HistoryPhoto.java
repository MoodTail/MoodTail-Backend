package com.example.moodtail.domain.history.entity;

import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "history_photos",
        indexes = {
                @Index(name = "idx_history_photo_user_date", columnList = "user_id,record_date"),
                @Index(name = "idx_history_photo_image", columnList = "image_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoryPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    private HistoryPhoto(User user, LocalDate recordDate, Image image) {
        this.user = user;
        this.recordDate = recordDate;
        this.image = image;
    }

    public static HistoryPhoto create(User user, LocalDate recordDate, Image image) {
        return new HistoryPhoto(user, recordDate, image);
    }
}
