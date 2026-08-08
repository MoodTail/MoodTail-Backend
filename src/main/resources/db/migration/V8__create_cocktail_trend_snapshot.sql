CREATE TABLE cocktail_trend_snapshot (
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    snapshot_data JSON     NOT NULL,
    updated_at    DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
