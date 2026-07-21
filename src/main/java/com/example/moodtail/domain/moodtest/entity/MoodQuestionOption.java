package com.example.moodtail.domain.moodtest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mood_question_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoodQuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mood_question_id", nullable = false)
    private MoodQuestion moodQuestion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    @OrderBy("metricType ASC")
    @OneToMany(mappedBy = "moodQuestionOption", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MoodQuestionOptionScore> scores = new ArrayList<>();
}
