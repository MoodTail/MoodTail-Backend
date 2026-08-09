package com.example.moodtail.domain.report.entity;

import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.global.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "monthly_report_shares")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyReportShare extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "share_token", nullable = false, unique = true, length = 64)
    private String shareToken;

    @Column(name = "report_year", nullable = false)
    private int reportYear;

    @Column(name = "report_month", nullable = false)
    private int reportMonth;

    @Column(name = "share_image_url", nullable = false, length = 2048)
    private String shareImageUrl;

    private MonthlyReportShare(
            User user,
            String shareToken,
            int reportYear,
            int reportMonth,
            String shareImageUrl
    ) {
        this.user = user;
        this.shareToken = shareToken;
        this.reportYear = reportYear;
        this.reportMonth = reportMonth;
        this.shareImageUrl = shareImageUrl;
    }

    public static MonthlyReportShare create(
            User user,
            String shareToken,
            int reportYear,
            int reportMonth,
            String shareImageUrl
    ) {
        return new MonthlyReportShare(
                user,
                shareToken,
                reportYear,
                reportMonth,
                shareImageUrl
        );
    }
}
