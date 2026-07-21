-- Core domain tables (users, cocktails) are Hibernate-managed and may be created after Flyway on an empty database.
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS shared_pair_recommendations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    share_token VARCHAR(64) NOT NULL,
    creator_user_id BIGINT NOT NULL,
    compromise_alcohol_intensity DECIMAL(2,1) NOT NULL,
    compromise_sweetness DECIMAL(2,1) NOT NULL,
    compromise_sourness DECIMAL(2,1) NOT NULL,
    compromise_refreshing DECIMAL(2,1) NOT NULL,
    compromise_bitterness DECIMAL(2,1) NOT NULL,
    cocktail_id_1 BIGINT NOT NULL,
    cocktail_id_2 BIGINT NOT NULL,
    cocktail_id_3 BIGINT NOT NULL,
    match_score_1 INT NOT NULL,
    match_score_2 INT NOT NULL,
    match_score_3 INT NOT NULL,
    my_match_score INT NOT NULL,
    partner_match_score INT NOT NULL,
    thumbnail_image_url VARCHAR(2048) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_shared_pair_recommendation_share_token UNIQUE (share_token),
    CONSTRAINT fk_shared_pair_recommendation_creator
        FOREIGN KEY (creator_user_id) REFERENCES users (id),
    CONSTRAINT fk_shared_pair_recommendation_cocktail_1
        FOREIGN KEY (cocktail_id_1) REFERENCES cocktails (id),
    CONSTRAINT fk_shared_pair_recommendation_cocktail_2
        FOREIGN KEY (cocktail_id_2) REFERENCES cocktails (id),
    CONSTRAINT fk_shared_pair_recommendation_cocktail_3
        FOREIGN KEY (cocktail_id_3) REFERENCES cocktails (id),
    CONSTRAINT chk_shared_pair_recommendation_compromise_alcohol_intensity
        CHECK (compromise_alcohol_intensity BETWEEN 1.0 AND 5.0),
    CONSTRAINT chk_shared_pair_recommendation_compromise_sweetness
        CHECK (compromise_sweetness BETWEEN 1.0 AND 5.0),
    CONSTRAINT chk_shared_pair_recommendation_compromise_sourness
        CHECK (compromise_sourness BETWEEN 1.0 AND 5.0),
    CONSTRAINT chk_shared_pair_recommendation_compromise_refreshing
        CHECK (compromise_refreshing BETWEEN 1.0 AND 5.0),
    CONSTRAINT chk_shared_pair_recommendation_compromise_bitterness
        CHECK (compromise_bitterness BETWEEN 1.0 AND 5.0),
    CONSTRAINT chk_shared_pair_recommendation_match_score_1
        CHECK (match_score_1 BETWEEN 0 AND 100),
    CONSTRAINT chk_shared_pair_recommendation_match_score_2
        CHECK (match_score_2 BETWEEN 0 AND 100),
    CONSTRAINT chk_shared_pair_recommendation_match_score_3
        CHECK (match_score_3 BETWEEN 0 AND 100),
    CONSTRAINT chk_shared_pair_recommendation_my_match_score
        CHECK (my_match_score BETWEEN 0 AND 100),
    CONSTRAINT chk_shared_pair_recommendation_partner_match_score
        CHECK (partner_match_score BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
