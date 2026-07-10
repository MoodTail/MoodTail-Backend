SET NAMES utf8mb4;

ALTER TABLE users
    MODIFY COLUMN email VARCHAR(100) NULL,
    MODIFY COLUMN nickname VARCHAR(50) NULL,
    MODIFY COLUMN password VARCHAR(255) NULL;

CREATE TABLE IF NOT EXISTS images (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    image_url VARCHAR(255) NOT NULL,
    source_type ENUM('SYSTEM', 'CAMERA', 'GALLERY') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO images (id, image_url, source_type)
VALUES
    (900001, 'https://cdn.moodtail.com/mock/types/type-0.png', 'SYSTEM'),
    (900002, 'https://cdn.moodtail.com/mock/types/type-1.png', 'SYSTEM'),
    (900003, 'https://cdn.moodtail.com/mock/types/type-2.png', 'SYSTEM'),
    (900004, 'https://cdn.moodtail.com/mock/types/type-3.png', 'SYSTEM'),
    (900005, 'https://cdn.moodtail.com/mock/types/type-4.png', 'SYSTEM'),
    (900006, 'https://cdn.moodtail.com/mock/types/type-5.png', 'SYSTEM'),
    (900007, 'https://cdn.moodtail.com/mock/types/type-6.png', 'SYSTEM'),
    (900008, 'https://cdn.moodtail.com/mock/types/type-7.png', 'SYSTEM'),
    (900009, 'https://cdn.moodtail.com/mock/types/type-8.png', 'SYSTEM'),
    (900010, 'https://cdn.moodtail.com/mock/types/type-9.png', 'SYSTEM'),
    (900011, 'https://cdn.moodtail.com/mock/types/type-10.png', 'SYSTEM'),
    (900012, 'https://cdn.moodtail.com/mock/types/type-11.png', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    image_url = VALUES(image_url),
    source_type = VALUES(source_type);

UPDATE mood_types
SET character_image_id = CASE id
    WHEN 2001 THEN 900001
    WHEN 2002 THEN 900002
    WHEN 2003 THEN 900003
    WHEN 2004 THEN 900004
    WHEN 2005 THEN 900005
    WHEN 2006 THEN 900006
    WHEN 2007 THEN 900007
    WHEN 2008 THEN 900008
    WHEN 2009 THEN 900009
    WHEN 2010 THEN 900010
    WHEN 2011 THEN 900011
    WHEN 2012 THEN 900012
    ELSE character_image_id
END
WHERE id BETWEEN 2001 AND 2012;
