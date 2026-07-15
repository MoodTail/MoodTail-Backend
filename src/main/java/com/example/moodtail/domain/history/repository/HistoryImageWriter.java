package com.example.moodtail.domain.history.repository;

import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.image.entity.ImageSourceType;
import com.example.moodtail.global.common.exception.RestApiException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

import static com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus._INTERNAL_SERVER_ERROR;

@Repository
@RequiredArgsConstructor
public class HistoryImageWriter {

    private static final String INSERT_IMAGE_SQL = "insert into images (image_url, source_type) values (?, ?)";

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    public Image insert(String imageUrl, ImageSourceType sourceType) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updated = jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_IMAGE_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, imageUrl);
            statement.setString(2, sourceType.name());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (updated != 1 || key == null) {
            throw new RestApiException(_INTERNAL_SERVER_ERROR);
        }
        return entityManager.getReference(Image.class, key.longValue());
    }

    public boolean deleteIfUnreferenced(Long imageId) {
        return jdbcTemplate.update("""
                delete from images
                 where id = ?
                   and not exists (select 1 from history_photos where image_id = ?)
                """, imageId, imageId) == 1;
    }
}
