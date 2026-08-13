-- MoodTail schema and reference-data baseline
-- Generated from the AWS MySQL schema and reference data on 2026-08-13.
-- Runtime/user-generated rows are intentionally excluded.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `cocktail_favorites` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cocktail_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cocktail_favorites_user_cocktail` (`user_id`,`cocktail_id`),
  KEY `FK9mqeb7c91ns4ct9vt1ngau3t9` (`cocktail_id`),
  CONSTRAINT `FK93bq447184ndonf3txk35m8rx` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FK9mqeb7c91ns4ct9vt1ngau3t9` FOREIGN KEY (`cocktail_id`) REFERENCES `cocktails` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `cocktail_ingredients` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount_text` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `sort_order` int NOT NULL,
  `cocktail_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbqdjf0b4va775bjo8ig7cyc2k` (`cocktail_id`),
  CONSTRAINT `FKbqdjf0b4va775bjo8ig7cyc2k` FOREIGN KEY (`cocktail_id`) REFERENCES `cocktails` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `cocktail_recipe_steps` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` text NOT NULL,
  `step_order` int NOT NULL,
  `cocktail_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfqqorvwygtbmww5x7ql4a1pju` (`cocktail_id`),
  CONSTRAINT `FKfqqorvwygtbmww5x7ql4a1pju` FOREIGN KEY (`cocktail_id`) REFERENCES `cocktails` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `cocktail_trend_snapshot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `snapshot_data` json NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `cocktails` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `alcohol_degree` decimal(4,1) DEFAULT NULL,
  `alcohol_intensity` decimal(10,9) NOT NULL,
  `bitterness` decimal(10,9) NOT NULL,
  `description` text,
  `image_id` bigint DEFAULT NULL,
  `name_en` varchar(100) NOT NULL,
  `name_ko` varchar(100) NOT NULL,
  `pairing_snack` varchar(255) DEFAULT NULL,
  `refreshing` decimal(10,9) NOT NULL,
  `short_description` varchar(255) DEFAULT NULL,
  `sourness` decimal(10,9) NOT NULL,
  `sweetness` decimal(10,9) NOT NULL,
  `mood_type_id` bigint NOT NULL,
  `base_spirit` enum('BRANDY','GIN','LIQUEUR','OTHER','RUM','TEQUILA','VODKA','WHISKEY','WINE') DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1rph72vir3b6cx6oopoo2rloq` (`name_en`),
  KEY `FK33haiirqlblycc9ri2xlyvq65` (`mood_type_id`),
  KEY `FKemoiqhe9tb8wh2883d1eq5rw3` (`image_id`),
  CONSTRAINT `FK33haiirqlblycc9ri2xlyvq65` FOREIGN KEY (`mood_type_id`) REFERENCES `mood_types` (`id`),
  CONSTRAINT `FKemoiqhe9tb8wh2883d1eq5rw3` FOREIGN KEY (`image_id`) REFERENCES `images` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `collection_shares` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `share_token` varchar(64) NOT NULL,
  `thumbnail_image_url` varchar(2048) NOT NULL,
  `version` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_collection_share_user` (`user_id`),
  UNIQUE KEY `uk_collection_share_token` (`share_token`),
  CONSTRAINT `FKm2jiugna7ekey1dm9lag3oee0` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `daily_cocktail_recommendations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cosine_similarity` double NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `humidity` int NOT NULL,
  `recommendation_date` date NOT NULL,
  `temperature` double NOT NULL,
  `weather` enum('CLEAR','CLOUDY','RAIN','SNOW') NOT NULL,
  `cocktail_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_daily_cocktail_recommendation_date` (`recommendation_date`),
  KEY `FKtlt491kdcpskvpek49xtmgot7` (`cocktail_id`),
  CONSTRAINT `FKtlt491kdcpskvpek49xtmgot7` FOREIGN KEY (`cocktail_id`) REFERENCES `cocktails` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `drinking_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `cocktail_id` bigint NOT NULL,
  `record_date` date NOT NULL,
  `recorded_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_drinking_record_user_date_cocktail` (`user_id`,`record_date`,`cocktail_id`),
  KEY `idx_drinking_record_cocktail` (`cocktail_id`),
  CONSTRAINT `fk_drinking_record_cocktail` FOREIGN KEY (`cocktail_id`) REFERENCES `cocktails` (`id`),
  CONSTRAINT `fk_drinking_record_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `history_photos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `record_date` date NOT NULL,
  `image_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_history_photo_user_date` (`user_id`,`record_date`),
  KEY `idx_history_photo_image` (`image_id`),
  CONSTRAINT `fk_history_photo_image` FOREIGN KEY (`image_id`) REFERENCES `images` (`id`),
  CONSTRAINT `fk_history_photo_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `image_url` varchar(255) NOT NULL,
  `source_type` enum('CAMERA','GALLERY','SYSTEM') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `inquiries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contact_email` varchar(255) DEFAULT NULL,
  `content` text NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `inquiry_type` enum('ACCOUNT','BUG','ETC','FEEDBACK') NOT NULL,
  `status` enum('PENDING','RESOLVED') NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfks94q8sobcuibrudbr3im380` (`user_id`),
  CONSTRAINT `FKfks94q8sobcuibrudbr3im380` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `local_accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `email` varchar(320) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `password_version` int NOT NULL DEFAULT '0',
  `failed_login_attempts` int NOT NULL DEFAULT '0',
  `locked_until` datetime DEFAULT NULL,
  `password_changed_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_local_account_user` (`user_id`),
  UNIQUE KEY `uk_local_account_email` (`email`),
  CONSTRAINT `fk_local_account_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `monthly_report_shares` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `report_month` int NOT NULL,
  `report_year` int NOT NULL,
  `share_image_url` varchar(2048) NOT NULL,
  `share_token` varchar(64) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqrroj1xlq1b8u7slx6i92m1xc` (`share_token`),
  KEY `FK8flh2deg63sgx2vcqr6ltu3oh` (`user_id`),
  CONSTRAINT `FK8flh2deg63sgx2vcqr6ltu3oh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `mood_question_option_scores` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `metric_type` enum('ALCOHOL_INTENSITY','BITTERNESS','REFRESHING','SOURNESS','SWEETNESS') NOT NULL,
  `score_type` enum('ABSOLUTE','DELTA') NOT NULL,
  `score_value` decimal(3,2) NOT NULL,
  `mood_question_option_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mood_question_option_metric` (`mood_question_option_id`,`metric_type`),
  CONSTRAINT `FK2bqsvyaumjopsijxp607k2fw1` FOREIGN KEY (`mood_question_option_id`) REFERENCES `mood_question_options` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `mood_question_options` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL,
  `option_order` int NOT NULL,
  `mood_question_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpk1vdwtuuhk0644veaw19ucdi` (`mood_question_id`),
  CONSTRAINT `FKpk1vdwtuuhk0644veaw19ucdi` FOREIGN KEY (`mood_question_id`) REFERENCES `mood_questions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `mood_questions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL,
  `is_active` bit(1) NOT NULL,
  `question_type` enum('FIXED','RANDOM') NOT NULL,
  `sort_order` int NOT NULL,
  `subtitle` text NOT NULL,
  `title` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `mood_test_results` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `alcohol_intensity` decimal(5,4) NOT NULL,
  `bitterness` decimal(5,4) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `refreshing` decimal(5,4) NOT NULL,
  `result_date` date NOT NULL,
  `share_token` varchar(255) DEFAULT NULL,
  `sourness` decimal(5,4) NOT NULL,
  `sweetness` decimal(5,4) NOT NULL,
  `mood_type_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mood_test_result_user_date` (`user_id`,`result_date`),
  UNIQUE KEY `UKk3dah6343no40wnfp87odqx8c` (`share_token`),
  KEY `FKj4qh1pb43ac8xjpequ2o2tyq8` (`mood_type_id`),
  CONSTRAINT `FKj4qh1pb43ac8xjpequ2o2tyq8` FOREIGN KEY (`mood_type_id`) REFERENCES `mood_types` (`id`),
  CONSTRAINT `FKt139k3k0tau5vi5kbpce727ld` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `mood_type_compatibilities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `compatibility_type` enum('BEST','WORST') NOT NULL,
  `mood_type_id` bigint NOT NULL,
  `target_mood_type_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKoyd8bk4mxsody2g70u6l1i7cm` (`mood_type_id`),
  KEY `FKosu5rnryrvuohy6b5tytk12gp` (`target_mood_type_id`),
  CONSTRAINT `FKosu5rnryrvuohy6b5tytk12gp` FOREIGN KEY (`target_mood_type_id`) REFERENCES `mood_types` (`id`),
  CONSTRAINT `FKoyd8bk4mxsody2g70u6l1i7cm` FOREIGN KEY (`mood_type_id`) REFERENCES `mood_types` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `mood_types` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `alcohol_intensity` decimal(4,3) NOT NULL,
  `bitterness` decimal(4,3) NOT NULL,
  `character_image_id` bigint DEFAULT NULL,
  `character_quote` varchar(255) DEFAULT NULL,
  `code` varchar(50) NOT NULL,
  `description` text,
  `name` varchar(100) NOT NULL,
  `refreshing` decimal(4,3) NOT NULL,
  `short_description` varchar(255) DEFAULT NULL,
  `sort_order` int NOT NULL,
  `sourness` decimal(4,3) NOT NULL,
  `sweetness` decimal(4,3) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqx1kpo2iw9hf0bt3byagktr4m` (`code`),
  KEY `FK5tn85ok2uxaeufxpml4ng0568` (`character_image_id`),
  CONSTRAINT `FK5tn85ok2uxaeufxpml4ng0568` FOREIGN KEY (`character_image_id`) REFERENCES `images` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `recommendation_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `match_score` int NOT NULL,
  `ranking` int NOT NULL,
  `cocktail_id` bigint NOT NULL,
  `recommendation_session_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recommendation_item_session_ranking` (`recommendation_session_id`,`ranking`),
  UNIQUE KEY `uk_recommendation_item_session_cocktail` (`recommendation_session_id`,`cocktail_id`),
  KEY `FKo8vtq7x06ramty6pdplia2s5i` (`cocktail_id`),
  CONSTRAINT `FKo8vtq7x06ramty6pdplia2s5i` FOREIGN KEY (`cocktail_id`) REFERENCES `cocktails` (`id`),
  CONSTRAINT `FKsf8tl8qfm1r6f4tpfe096f5k8` FOREIGN KEY (`recommendation_session_id`) REFERENCES `recommendation_sessions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `recommendation_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `session_type` enum('COMPROMISE','CUSTOM_TASTE','TEST_RESULT','TODAY') NOT NULL,
  `mood_test_result_id` bigint DEFAULT NULL,
  `partner_mood_test_result_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcq5b2nbrnsqqs53q1ufuk2ag7` (`partner_mood_test_result_id`),
  KEY `FKcgueuovvm2vwwqhk8l8ngsykd` (`user_id`),
  KEY `idx_recommendation_session_result_type` (`mood_test_result_id`,`session_type`),
  CONSTRAINT `FKcgueuovvm2vwwqhk8l8ngsykd` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKcq5b2nbrnsqqs53q1ufuk2ag7` FOREIGN KEY (`partner_mood_test_result_id`) REFERENCES `mood_test_results` (`id`),
  CONSTRAINT `FKdtjdg96in32muj4busl6xarrj` FOREIGN KEY (`mood_test_result_id`) REFERENCES `mood_test_results` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shared_mood_test_results` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `alcohol_intensity` decimal(5,4) NOT NULL,
  `bitterness` decimal(5,4) NOT NULL,
  `refreshing` decimal(5,4) NOT NULL,
  `share_token` varchar(64) NOT NULL,
  `sourness` decimal(5,4) NOT NULL,
  `sweetness` decimal(5,4) NOT NULL,
  `thumbnail_image_url` varchar(2048) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKi6h8iddlioe7xfrpl3yklifh4` (`share_token`),
  KEY `FKq5r0n8oo0y8858bgxm9y1nx5d` (`user_id`),
  CONSTRAINT `FKq5r0n8oo0y8858bgxm9y1nx5d` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shared_pair_recommendations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `share_token` varchar(64) NOT NULL,
  `creator_user_id` bigint NOT NULL,
  `compromise_alcohol_intensity` decimal(5,4) NOT NULL,
  `compromise_sweetness` decimal(5,4) NOT NULL,
  `compromise_sourness` decimal(5,4) NOT NULL,
  `compromise_refreshing` decimal(5,4) NOT NULL,
  `compromise_bitterness` decimal(5,4) NOT NULL,
  `cocktail_id_1` bigint NOT NULL,
  `cocktail_id_2` bigint NOT NULL,
  `cocktail_id_3` bigint NOT NULL,
  `match_score_1` int NOT NULL,
  `match_score_2` int NOT NULL,
  `match_score_3` int NOT NULL,
  `my_match_score` int NOT NULL,
  `partner_match_score` int NOT NULL,
  `thumbnail_image_url` varchar(2048) NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shared_pair_recommendation_share_token` (`share_token`),
  KEY `fk_shared_pair_recommendation_creator` (`creator_user_id`),
  KEY `fk_shared_pair_recommendation_cocktail_1` (`cocktail_id_1`),
  KEY `fk_shared_pair_recommendation_cocktail_2` (`cocktail_id_2`),
  KEY `fk_shared_pair_recommendation_cocktail_3` (`cocktail_id_3`),
  CONSTRAINT `fk_shared_pair_recommendation_cocktail_1` FOREIGN KEY (`cocktail_id_1`) REFERENCES `cocktails` (`id`),
  CONSTRAINT `fk_shared_pair_recommendation_cocktail_2` FOREIGN KEY (`cocktail_id_2`) REFERENCES `cocktails` (`id`),
  CONSTRAINT `fk_shared_pair_recommendation_cocktail_3` FOREIGN KEY (`cocktail_id_3`) REFERENCES `cocktails` (`id`),
  CONSTRAINT `fk_shared_pair_recommendation_creator` FOREIGN KEY (`creator_user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `chk_shared_pair_recommendation_compromise_alcohol_intensity` CHECK ((`compromise_alcohol_intensity` between 1.0 and 5.0)),
  CONSTRAINT `chk_shared_pair_recommendation_compromise_bitterness` CHECK ((`compromise_bitterness` between 1.0 and 5.0)),
  CONSTRAINT `chk_shared_pair_recommendation_compromise_refreshing` CHECK ((`compromise_refreshing` between 1.0 and 5.0)),
  CONSTRAINT `chk_shared_pair_recommendation_compromise_sourness` CHECK ((`compromise_sourness` between 1.0 and 5.0)),
  CONSTRAINT `chk_shared_pair_recommendation_compromise_sweetness` CHECK ((`compromise_sweetness` between 1.0 and 5.0)),
  CONSTRAINT `chk_shared_pair_recommendation_match_score_1` CHECK ((`match_score_1` between 0 and 100)),
  CONSTRAINT `chk_shared_pair_recommendation_match_score_2` CHECK ((`match_score_2` between 0 and 100)),
  CONSTRAINT `chk_shared_pair_recommendation_match_score_3` CHECK ((`match_score_3` between 0 and 100)),
  CONSTRAINT `chk_shared_pair_recommendation_my_match_score` CHECK ((`my_match_score` between 0 and 100)),
  CONSTRAINT `chk_shared_pair_recommendation_partner_match_score` CHECK ((`partner_match_score` between 0 and 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `social_accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `provider` varchar(20) NOT NULL,
  `provider_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `email` varchar(320) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_social_provider_user` (`provider`,`provider_user_id`),
  UNIQUE KEY `uk_social_user_provider` (`user_id`,`provider`),
  CONSTRAINT `fk_social_account_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `terms` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `term_type` varchar(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `is_required` tinyint(1) NOT NULL DEFAULT '1',
  `version` varchar(255) NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_terms_type_version` (`term_type`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_term_agreements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `term_id` bigint NOT NULL,
  `agreed_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_term_agreement` (`user_id`,`term_id`),
  KEY `fk_user_term_agreement_term` (`term_id`),
  CONSTRAINT `fk_user_term_agreement_term` FOREIGN KEY (`term_id`) REFERENCES `terms` (`id`),
  CONSTRAINT `fk_user_term_agreement_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_unlocked_cocktails` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `unlocked_at` datetime(6) NOT NULL,
  `cocktail_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_unlocked_cocktail_user_cocktail` (`user_id`,`cocktail_id`),
  KEY `FKnocolrd9320b2kyfcx6p869r9` (`cocktail_id`),
  CONSTRAINT `FKhb8xiv3f9jg6jgq0c8t19ymui` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKnocolrd9320b2kyfcx6p869r9` FOREIGN KEY (`cocktail_id`) REFERENCES `cocktails` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_unlocked_mood_types` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `unlocked_at` datetime(6) NOT NULL,
  `mood_type_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_unlocked_mood_type_user_type` (`user_id`,`mood_type_id`),
  KEY `FKiakngg9olridg7why1cfqd2gn` (`mood_type_id`),
  CONSTRAINT `FKiakngg9olridg7why1cfqd2gn` FOREIGN KEY (`mood_type_id`) REFERENCES `mood_types` (`id`),
  CONSTRAINT `FKmmem4itvhcn5m8jpjm60sl2l3` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(20) NOT NULL,
  `guest_uuid` varchar(36) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `last_accessed_at` datetime NOT NULL,
  `invite_code` varchar(20) DEFAULT NULL,
  `representative_mood_type_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `uk_users_guest_uuid` (`guest_uuid`),
  UNIQUE KEY `uk_users_invite_code` (`invite_code`),
  KEY `idx_users_status_deleted` (`status`,`deleted_at`),
  KEY `idx_users_mood_type_status_deleted` (`representative_mood_type_id`,`status`,`deleted_at`),
  CONSTRAINT `FKdw2j1l158umhjijiv4fmrs866` FOREIGN KEY (`representative_mood_type_id`) REFERENCES `mood_types` (`id`),
  CONSTRAINT `ck_users_guest_identity` CHECK ((((`role` = _utf8mb4'GUEST') and (`guest_uuid` is not null)) or ((`role` in (_utf8mb4'USER',_utf8mb4'ADMIN')) and (`guest_uuid` is null))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Reference data
-- Only service-managed reference rows are included.
-- User/runtime-generated rows are intentionally excluded.

-- images: 130 reference rows
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('1','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/balanced-mediator.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('3','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/easygoing-optimist.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('4','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/emotional-thinker.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('5','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/explosive-adventurer.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('6','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/free-spirited-romantic.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('7','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/grounded-realist.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('8','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/meticulous-critic.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('9','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/passionate-challenger.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('10','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/quiet-supporter.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('11','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/refreshing-explorer.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('12','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/sensitive-perfectionist.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('13','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/steadfast-principlist.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('26','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/WHITE%20LADY.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('27','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/ZOMBIE.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('28','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/VE.N.TO.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('29','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/VESPER.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('30','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/VIEUX%20CARRE.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('31','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/WHISKEY%20SOUR.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('32','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/STINGER.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('33','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/SUGGERING%20BASTARD.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('34','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/TEQUILA%20SUNRISE.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('35','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/THREE%20DOTS%20AND%20A%20DASH.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('36','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/TIPPERARY.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('37','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/TOMMY%27S%20MARGARITA.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('38','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/TRINIDAD%20SOUR.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('39','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/TUXEDO.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('40','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/SEX%20ON%20THE%20BEACH.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('41','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/SHERRY%20COBBLER.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('42','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/SIDECAR.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('43','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/SINGAPORE%20SLING.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('44','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/SOUTH%20SIDE.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('45','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/SPICY%20FIFTY.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('46','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/SPRITZ.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('47','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/REMEMBER%20THE%20MAINE.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('48','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/RUSSIAN%20SPRING%20PUNCH.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('49','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/RUSTY%20NAIL.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('50','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/SAZERAC.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('51','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/PINA%20COLADA.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('52','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/PISCO%20PUNCH.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('53','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/PISCO%20SOUR.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('54','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/PLANTERS%20PUNCH.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('55','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/PORN%20STAR%20MARTINI.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('56','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/PORTO%20FLIP.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('57','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/RABO%20DE%20GALO.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('58','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/NEGRONI.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('59','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/NEW%20YORK%20SOUR.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('60','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/OLD%20CUBAN.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('61','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/OLD%20FASHIONED.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('62','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/PALOMA.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('63','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/PAPER%20PLANE.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('64','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/PARADISE.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('65','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/PENICILLIN.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('66','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/MOJITO.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('67','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/MONKEY%20GLAND.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('68','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/MOSCOW%20MULE.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('69','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/NAKED%20AND%20FAMOUS.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('70','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/MAI-TAI.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('71','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/MANHATTAN.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('72','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/MARGARITA.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('73','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/MARTINEZ.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('74','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/MARY%20PICKFORD.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('75','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/MIMOSA.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('76','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/MINT%20JULEP.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('77','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/MISSIONARY%27S%20DOWNFALL.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('78','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/ILLEGAL.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('79','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/IRISH%20COFFEE.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('80','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/JOHN%20COLLINS.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('81','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/JUNGLE%20BIRD.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('82','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/KIR.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('83','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/LAST%20WORD.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('84','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/LEMON%20DROP%20MARTINI.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('85','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/LONG%20ISLAND%20ICED%20TEA.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('86','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/DON%27S%20SPECIAL%20DAIQUIRI.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('87','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/DRY%20MARTINI.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('88','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/ESPRESSO%20MARTINI.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('89','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/FERNANDITO.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('90','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/FRENCH%2075.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('91','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/FRENCH%20CONNECTION.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('92','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/FRENCH%20MARTINI.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('93','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/GIN%20BASIL%20SMASH.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('94','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/GIN%20FIZZ.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('95','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/GRAND%20MARGARITA.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('96','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/GRASSHOPPER.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('97','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/HANKY%20PANKY.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('98','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/HEMINGWAY%20SPECIAL.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('99','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/HORSE%27S%20NECK.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('100','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/ALEXANDER.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('101','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/AMERICANO.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('102','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/ANGEL%20FACE.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('103','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/AVIATION.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('104','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/BEE%27S%20KNEES.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('105','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/BELLINI.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('106','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/BETWEEN%20THE%20SHEETS.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('107','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/BLACK%20RUSSIAN.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('108','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/BLOODY%20MARY.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('109','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/BOULEVADIER.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('110','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/BRAMBLE.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('111','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/BRANDY%20CRUSTA.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('112','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/CAIPIRINHA.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('113','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/CANCHANCHARA.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('114','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/CARDINALE.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('115','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/CASINO.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('116','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/CHARTREUSE%20SWIZZLE.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('117','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/CLOVER%20CLUB.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('118','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/CORPSE%20REVIVER%20%232.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('119','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/COSMOPOLITAN.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('120','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/DAIQUIRI.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('121','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/DARK%20%27N%27%20STORMY.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('124','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/irish_coffee_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('125','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/tumbler_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('126','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/wide_coupe_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('127','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/beer_mug.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('128','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/brandy_snifter.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('129','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/champagne_flute.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('130','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/cocktail_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('131','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/cognac_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('132','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/coupe_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('133','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/glass_mug.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('134','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/glencairn_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('135','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/highball_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('136','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/hurricane_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('137','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/margarita_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('138','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/martini_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('139','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/pint_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('140','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/rocks_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('141','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/shot_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('142','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/sour_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('143','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/whiskey_tumbler.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('144','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/white_wine_glass.png','SYSTEM');
INSERT INTO `images` (`id`,`image_url`,`source_type`) VALUES ('145','https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/glasses/wine_glass.png','SYSTEM');

-- mood_types: 12 reference rows
INSERT INTO `mood_types` (`alcohol_intensity`,`bitterness`,`character_image_id`,`character_quote`,`code`,`description`,`id`,`name`,`refreshing`,`short_description`,`sort_order`,`sourness`,`sweetness`) VALUES ('3.597','1.458','7','묵직한 한 잔이 오래 남는 법이지.','grounded-realist','화려한 가니쉬보다 잘 숙성된 베이스 한 모금을 더 즐기는 타입이에요. 팔짱을 끼고 조용히 앉아 있어도 존재감이 느껴지죠. 말수가 적은 편이지만, 입을 열면 핵심만 말해요. 단순해 보여도 깊이가 있고, 시간이 지날수록 진가를 알게 되는 타입이에요.','1','묵직한 현실주의자','1.000','클래식하고 깊은 맛처럼, 말보다 무게로 말하는 타입','1','1.959','1.765');
INSERT INTO `mood_types` (`alcohol_intensity`,`bitterness`,`character_image_id`,`character_quote`,`code`,`description`,`id`,`name`,`refreshing`,`short_description`,`sort_order`,`sourness`,`sweetness`) VALUES ('2.125','1.431','9','망설일 시간에 한 잔 더!','passionate-challenger','달콤하고 상큼한 맛에 거침없이 손을 뻗는 타입이에요. 새로운 칵테일 앞에서도 망설임 없이 "이거 마셔볼게요"를 외치죠. 오늘도 전속력으로 달리는 중이지만, 표정은 언제나 해맑아요. 에너지가 넘쳐서 가끔 주변 사람들이 지칠 때도 있지만 그 열기에 결국 모두가 같이 달리게 되는 타입이에요.','2','열정적인 도전자','2.375','달콤하고 상큼한 것에 끌리는, 언제나 먼저 달려가는 타입','2','2.803','3.024');
INSERT INTO `mood_types` (`alcohol_intensity`,`bitterness`,`character_image_id`,`character_quote`,`code`,`description`,`id`,`name`,`refreshing`,`short_description`,`sort_order`,`sourness`,`sweetness`) VALUES ('2.598','1.475','12','이 한 방울까지 계산된 선택이야','sensitive-perfectionist','피스코 사워의 산미가 정확히 몇 퍼센트인지 느낄 수 있는 타입이에요. 날카롭게 뻗은 마티니 글라스처럼, 실루엣부터 취향이 분명해요. "그냥 아무거나"는 이 사람의 사전에 없는 말이에요. 디테일을 놓치지 않고, 한 번 틀어진 게 계속 신경 쓰이는 편이지만 그 섬세함 덕분에 결과물은 항상 완벽에 가까워요.','3','예민한 완벽주의자','1.000','날카로운 산미처럼, 디테일 하나도 그냥 넘기지 않는 타입','3','2.645','2.123');
INSERT INTO `mood_types` (`alcohol_intensity`,`bitterness`,`character_image_id`,`character_quote`,`code`,`description`,`id`,`name`,`refreshing`,`short_description`,`sort_order`,`sourness`,`sweetness`) VALUES ('3.843','2.883','13','기준이 있어야 선택도 의미 있어','steadfast-principlist','네그로니의 쓴맛을 즐길 줄 아는 타입이에요. 남들이 "이거 너무 쓰다"고 할 때도, 오히려 한 모금을 더 음미하죠. 자신만의 기준이 분명하고 쉽게 타협하지 않아요. 차갑게 보일 때도 있지만, 믿음직스럽고 오래 알수록 의외의 따뜻함이 느껴지는 사람이에요.','4','진중한 원칙주의자','1.000','쓴맛도 즐길 줄 아는, 타협 없이 자신의 기준을 지키는 타입','4','1.255','2.086');
INSERT INTO `mood_types` (`alcohol_intensity`,`bitterness`,`character_image_id`,`character_quote`,`code`,`description`,`id`,`name`,`refreshing`,`short_description`,`sort_order`,`sourness`,`sweetness`) VALUES ('1.846','2.176','6','안 마셔본 거? 그걸로!','free-spirited-romantic','그날의 기분에 어울리는 한 잔을 고르는 시간이 가장 설레는 타입이에요. 오늘은 상그리아, 내일은 모스코 뮬. 우산 꽂힌 트로피컬 잔처럼 어디서든 분위기를 만들고, 평범한 하루도 특별한 추억으로 바꾸죠. 새로운 경험을 사랑하지만, 결국 기억에 남는 건 함께한 순간이에요.','5','자유로운 낭만주의자','3.250','탄산처럼 톡 튀고 다채로운, 어디서든 분위기를 만드는 타입','5','2.291','2.663');
INSERT INTO `mood_types` (`alcohol_intensity`,`bitterness`,`character_image_id`,`character_quote`,`code`,`description`,`id`,`name`,`refreshing`,`short_description`,`sort_order`,`sourness`,`sweetness`) VALUES ('4.368','1.760','5','한 번 시작했으면 끝까지!','explosive-adventurer','도수가 세다는 말을 들을수록 더 궁금해지는 타입이에요. 불꽃이 머리 위에서 타오르고 있어도 본인은 그게 일상이에요. 한 번 마음먹으면 끝까지 가고, 중간에 돌아서는 법이 없어요. 강렬한 도수처럼 처음엔 압도되지만, 적응하면 이 사람 없이는 심심해지죠. 극단적이지만 그게 매력인 타입이에요.','6','폭발적인 모험가','1.000','강렬하고 극단적인, 한 번 꽂히면 끝까지 밀어붙이는 타입','6','1.109','1.682');
INSERT INTO `mood_types` (`alcohol_intensity`,`bitterness`,`character_image_id`,`character_quote`,`code`,`description`,`id`,`name`,`refreshing`,`short_description`,`sort_order`,`sourness`,`sweetness`) VALUES ('2.702','2.848','8','생각보다 재밌는 조합이네','meticulous-critic','에스프레소 마티니처럼 한 모금에도 다양한 맛을 발견하는 타입이에요. 작은 차이도 그냥 지나치지 않고, 자신만의 기준으로 하나하나 살펴보죠. 디테일을 보는 눈이 뛰어나고, 좋은 건 좋다고, 아쉬운 건 아쉽다고 솔직하게 말해요. 까다로워 보일 수 있지만 그만큼 보는 눈이 정확한 타입이에요.','7','꼼꼼한 평론가','1.000','작은 차이도 놓치지 않는, 자신만의 기준으로 맛을 살펴보는 타입','7','2.307','2.397');
INSERT INTO `mood_types` (`alcohol_intensity`,`bitterness`,`character_image_id`,`character_quote`,`code`,`description`,`id`,`name`,`refreshing`,`short_description`,`sort_order`,`sourness`,`sweetness`) VALUES ('2.012','1.701','11','가볍게 한 잔, 가볍게 한 걸음!','refreshing-explorer','모히토 한 잔만 있으면 새로운 장소도, 새로운 메뉴도 망설임 없이 도전하는 타입이에요. 가벼운 발걸음으로 다양한 경험을 즐기고, 익숙한 것보다 새로운 것을 만날 때 더 설레죠. 언제나 청량한 에너지로 주변까지 활기차게 만드는 사람이에요.','8','청량한 탐험가','2.625','시원한 호기심으로 새로운 즐거움을 찾아가는 타입','8','2.170','1.839');
INSERT INTO `mood_types` (`alcohol_intensity`,`bitterness`,`character_image_id`,`character_quote`,`code`,`description`,`id`,`name`,`refreshing`,`short_description`,`sort_order`,`sourness`,`sweetness`) VALUES ('2.357','1.479','3','괜찮아, 결국엔 다 잘 풀릴 거야!','easygoing-optimist','테킬라 선라이즈처럼 보기만 해도 기분이 좋아지는 타입이에요. 달콤하고 화사한 과일향처럼 주변에 따뜻한 기운을 퍼뜨리죠. 눈을 감고 힐링 중인 모습이 가장 자연스럽고, 급하지 않아요. 다 잘 될 거라는 걸 본능적으로 알고 있거든요. 이 사람 옆에 있으면 왠지 덩달아 여유로워지는 타입이에요.','9','여유로운 낙관자','1.000','달콤하고 화사한 과일향처럼, 어디서나 따뜻한 기운을 주는 타입','9','2.858','2.584');
INSERT INTO `mood_types` (`alcohol_intensity`,`bitterness`,`character_image_id`,`character_quote`,`code`,`description`,`id`,`name`,`refreshing`,`short_description`,`sort_order`,`sourness`,`sweetness`) VALUES ('3.601','1.849','10','말은 없어도 언제나 네 편이야','quiet-supporter','화려하게 나서진 않지만, 필요한 순간엔 가장 먼저 손을 내미는 타입이에요. 말보다 행동으로 마음을 전하고, 언제나 묵묵하게 곁을 지켜주죠. 오래 함께할수록 든든함이 더 크게 느껴지는 사람이에요.','10','조용한 지지자','1.500','부드러운 온기처럼, 곁에서 조용히 힘이 되어주는 타입','10','1.517','2.169');
INSERT INTO `mood_types` (`alcohol_intensity`,`bitterness`,`character_image_id`,`character_quote`,`code`,`description`,`id`,`name`,`refreshing`,`short_description`,`sort_order`,`sourness`,`sweetness`) VALUES ('2.719','1.399','1','모두가 만족하는 한 잔이면 충분해','balanced-mediator','위스키 사워처럼 달고 시고 쓴 게 딱 균형 잡힌 타입이에요. 어느 한쪽으로 치우치지 않고, 모든 자리에 자연스럽게 녹아들어요. 갈등이 생기면 자연스럽게 가운데서 조율하고 있는 게 이 사람이에요. 가장 무난해 보이지만, 실은 가장 없어서는 안 될 타입이에요.','11','균형적인 중재자','1.000','어느 한쪽으로 치우치지 않는, 모든 자리에 자연스럽게 녹아드는 타입','11','1.896','2.360');
INSERT INTO `mood_types` (`alcohol_intensity`,`bitterness`,`character_image_id`,`character_quote`,`code`,`description`,`id`,`name`,`refreshing`,`short_description`,`sort_order`,`sourness`,`sweetness`) VALUES ('3.274','2.666','4','기분을 이 잔에 담아둘래','emotional-thinker','잔을 들고 조용히 생각에 잠겨 있는 모습이 가장 잘 어울리는 타입이에요. 혼자만의 시간을 즐기며 오늘의 감정과 생각을 차분히 정리하죠. 쉽게 속마음을 드러내지는 않지만, 세상을 누구보다 깊이 바라보고 오래 기억하는 사람이에요.','12','감성적인 사색가','1.000','복잡하고 깊은 여운처럼, 혼자만의 세계에서 사색하는 타입','12','1.736','2.093');

-- mood_type_compatibilities: 24 reference rows
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('BEST','1','1','10');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('WORST','2','1','5');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('BEST','3','2','8');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('WORST','4','2','6');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('BEST','5','3','9');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('WORST','6','3','4');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('BEST','7','4','6');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('WORST','8','4','3');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('BEST','9','5','8');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('WORST','10','5','1');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('BEST','11','6','4');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('WORST','12','6','2');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('BEST','13','7','12');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('WORST','14','7','11');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('BEST','15','8','5');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('WORST','16','8','12');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('BEST','17','9','3');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('WORST','18','9','10');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('BEST','19','10','1');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('WORST','20','10','9');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('BEST','21','11','9');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('WORST','22','11','7');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('BEST','23','12','7');
INSERT INTO `mood_type_compatibilities` (`compatibility_type`,`id`,`mood_type_id`,`target_mood_type_id`) VALUES ('WORST','24','12','8');

-- cocktails: 96 reference rows
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('29.5','3.659279778','GIN','1.457142857',NULL,'1','103','1','Aviation','아비에이션',NULL,'1.000000000','강렬한 타격감의 진 베이스로, 잔잔한 단맛과 싱그러운 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','1.800000000','1.553191489');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('29.7','3.681440443','BRANDY','1.528571429',NULL,'2','111','1','Brandy Crusta','브랜디 크러스타',NULL,'1.000000000','강렬한 새콤함 산뜻함에 진한 달콤함을 더한 묵직한 고도수의 묵직한 쓴맛 칵테일','2.042424242','2.008510638');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('31.9','3.925207756','GIN','1.445714286',NULL,'3','115','1','Casino','카지노',NULL,'1.000000000','화사한 달콤함 베이스에 부드러운 산뜻함과 짙은 여운이 어우러진 도수 높고 진한 칵테일','1.543030303','1.604255319');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('26.7','3.349030471','RUM','1.342857143',NULL,'4','120','1','Daiquiri','다이키리',NULL,'1.000000000','은근한 무게감의 럼 베이스로, 은은한 단맛과 싱그러운 산미가 조화를 이룬 은은한 쌉싸름함 칵테일','1.969696970','1.723404255');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('27.7','3.459833795','VODKA','1.114285714',NULL,'5','84','1','Lemon Drop Martini','레몬 드롭 마티니',NULL,'1.000000000','상큼한 풍미 산뜻함에 기분 좋은 단맛을 더한 칵테일','1.824242424','1.617021277');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('29.4','3.648199446','TEQUILA','1.685714286',NULL,'6','72','1','Margarita','마가리타',NULL,'1.000000000','화사한 달콤함 베이스에 톡 쏘는 신맛과 짙은 여운이 어우러진 도수 높고 진한 칵테일','2.066666667','1.617021277');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('28.8','3.581717452','BRANDY','1.685714286',NULL,'7','42','1','Sidecar','사이드카',NULL,'1.000000000','은근한 무게감의 브랜디 베이스로, 녹진한 단맛과 산미가 조화를 이룬 칵테일','2.309090909','2.063829787');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('27.8','3.470914127','GIN','1.400000000',NULL,'8','26','1','White Lady','화이트 레이디',NULL,'1.000000000','강렬한 새콤함 산뜻함에 기분 좋은 단맛을 더한 묵직한 쓴맛 칵테일','2.115151515','1.936170213');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('24.6','3.116343490','GIN','1.725714286',NULL,'9','118','2','Corpse Reviver #2','코프스 리바이버 #2',NULL,'1.000000000','묵직한 단맛 베이스에 톡 쏘는 신맛과 짙은 여운이 어우러진 목 넘김이 편한 칵테일','2.745454545','2.365957447');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('15.6','2.119113573','OTHER','1.685714286',NULL,'10','85','2','Long Island Iced Tea','롱 아일랜드 아이스 티',NULL,'3.000000000','부드럽고 매끄러운 진 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','2.648484848','4.382978723');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('5.5','1.000000000','WINE','1.428571429',NULL,'11','75','2','Mimosa','미모사',NULL,'4.000000000','강렬한 새콤함 산뜻함에 진한 달콤함을 더한 산뜻하고 가벼운 묵직한 쓴맛 칵테일','3.545454545','2.872340426');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('18.3','2.418282548','RUM','1.142857143',NULL,'12','77','2','Missionary’s Downfall','미셔너리스 다운폴',NULL,'1.000000000','묵직한 단맛 베이스에 톡 쏘는 신맛과 깨끗한 끝맛이 어우러진 목 넘김이 편한 칵테일','2.090909091','3.021276596');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('16.4','2.207756233','RUM','1.754285714',NULL,'13','60','2','Old Cuban','올드 쿠반',NULL,'3.000000000','부드럽고 매끄러운 럼 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 칵테일','2.963636364','2.978723404');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('15.4','2.096952909','RUM','1.000000000',NULL,'14','51','2','Pina Colada','피나 콜라다',NULL,'1.000000000','상큼한 풍미 산뜻함에 진한 달콤함을 더한 깔끔한 밸런스의 투명한 뒷맛 칵테일','1.969696970','2.787234043');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('14.9','2.041551247','VODKA','1.285714286',NULL,'15','55','2','Porn Star Martini','포른 스타 마티니',NULL,'3.000000000','묵직한 단맛 베이스에 톡 쏘는 신맛과 은근한 뒷맛이 어우러진 목 넘김이 편한 칵테일','3.230303030','3.340425532');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('14.5','1.997229917','VODKA','1.428571429',NULL,'16','48','2','Russian Spring Punch','러시안 스프링 펀치',NULL,'3.000000000','부담 없이 가벼운 보드카 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 칵테일','3.230303030','2.446808511');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('19.5','2.551246537','GIN','1.300000000',NULL,'17','104','3','Bee’s Knees','비즈 니즈',NULL,'1.000000000','강렬한 새콤함 산뜻함에 기분 좋은 단맛을 더한 깔끔한 밸런스의 달콤 쌉쌀함 칵테일','2.527272727','1.819148936');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('22.1','2.839335180','GIN','1.371428571',NULL,'18','110','3','Bramble','브램블',NULL,'1.000000000','묵직한 단맛 베이스에 톡 쏘는 신맛과 은근한 뒷맛이 어우러진 목 넘김이 편한 칵테일','2.357575758','2.042553191');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('21.8','2.806094183','RUM','1.342857143',NULL,'19','112','3','Caipirinha','카이피리냐',NULL,'1.000000000','부드럽고 매끄러운 리큐르 베이스로, 녹진한 단맛과 산미가 조화를 이룬 칵테일','2.745454545','2.191489362');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('22.2','2.850415512','RUM','1.628571429',NULL,'20','98','3','Hemingway Special','헤밍웨이 스페셜',NULL,'1.000000000','강렬한 새콤함 산뜻함에 진한 달콤함을 더한 깔끔한 밸런스의 묵직한 쓴맛 칵테일','2.769696970','2.000000000');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('19.4','2.540166205','BRANDY','1.531428571',NULL,'21','52','3','Pisco Punch','피스코 펀치',NULL,'1.000000000','묵직한 단맛 베이스에 톡 쏘는 신맛과 짙은 여운이 어우러진 목 넘김이 편한 칵테일','2.890909091','2.553191489');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('17.1','2.285318560','BRANDY','1.342857143',NULL,'22','53','3','Pisco Sour','피스코 사워',NULL,'1.000000000','부드럽고 매끄러운 리큐르 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 은은한 쌉싸름함 칵테일','2.745454545','2.191489362');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('19.0','2.495844875','TEQUILA','1.685714286',NULL,'23','37','3','Tommy’s Margarita','토미스 마가리타',NULL,'1.000000000','강렬한 새콤함 산뜻함에 진한 달콤함을 더한 깔끔한 밸런스의 묵직한 쓴맛 칵테일','2.745454545','2.234042553');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('18.3','2.418282548','BRANDY','1.600000000',NULL,'24','28','3','Ve.N.To','벤토',NULL,'1.000000000','화사한 달콤함 베이스에 톡 쏘는 신맛과 짙은 여운이 어우러진 목 넘김이 편한 칵테일','2.381818182','1.948936170');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('28.6','3.559556787','WHISKEY','3.571428571',NULL,'25','109','4','Boulevardier','불레바디에',NULL,'1.000000000','은근한 무게감의 위스키 베이스로, 녹진한 단맛과 차분한 결이 조화를 이룬 깊은 쌉쌀함 칵테일','1.290909091','2.553191489');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('28.4','3.537396122','GIN','2.671428571',NULL,'26','97','4','Hanky Panky','핸키 팬키',NULL,'1.000000000','묵직한 맛 산뜻함에 기분 좋은 단맛을 더한 적당한 볼륨감의 묵직한 쓴맛 칵테일','1.218181818','1.946808511');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('27.7','3.459833795','GIN','2.445714286',NULL,'27','73','4','Martinez','마르티네스',NULL,'1.000000000','묵직한 단맛 베이스에 차분함과 짙은 여운이 어우러진 술맛이 확실한 칵테일','1.252121212','2.051063830');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('26.7','3.349030471','GIN','3.228571429',NULL,'28','58','4','Negroni','네그로니',NULL,'1.000000000','은근한 무게감의 진 베이스로, 은은한 단맛과 차분한 결이 조화를 이룬 깊은 쌉쌀함 칵테일','1.290909091','1.978723404');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('31.2','3.847645429','RUM','2.600000000',NULL,'29','57','4','Rabo de Galo','하보 드 갈로',NULL,'1.000000000','튀지 않는 산미에 기분 좋은 단맛을 더한 묵직한 고도수의 묵직한 쓴맛 칵테일','1.460606061','1.774468085');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('37.2','4.512465374','WHISKEY','2.931428571',NULL,'30','47','4','Remember the Maine','리멤버 더 메인',NULL,'1.000000000','묵직한 단맛 베이스에 차분함과 짙은 여운이 어우러진 도수 높고 진한 칵테일','1.111515152','2.170212766');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('35.8','4.357340720','WHISKEY','2.857142857',NULL,'31','36','4','Tipperary','티페러리',NULL,'1.000000000','강렬한 타격감의 위스키 베이스로, 녹진한 단맛과 차분한 결이 조화를 이룬 깊은 쌉쌀함 칵테일','1.121212121','2.114893617');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('33.7','4.124653740','WHISKEY','2.760000000',NULL,'32','30','4','Vieux Carré','뷰 카레',NULL,'1.000000000','묵직한 맛 산뜻함에 진한 달콤함을 더한 묵직한 고도수의 묵직한 쓴맛 칵테일','1.290909091','2.102127660');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('7.3','1.199445983','WINE','1.571428571',NULL,'33','105','5','Bellini','벨리니',NULL,'4.000000000','묵직한 단맛 베이스에 톡 쏘는 신맛과 짙은 여운이 어우러진 낮은 도수의 칵테일','2.939393939','2.872340426');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('16.1','2.174515235','RUM','2.828571429',NULL,'34','121','5','Dark ‘N’ Stormy','다크 앤 스토미',NULL,'4.000000000','부드럽고 매끄러운 진 베이스로, 녹진한 단맛과 싱그러운 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','1.969696970','3.000000000');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('16.7','2.240997230','LIQUEUR','1.342857143',NULL,'35','96','5','Grasshopper','그래스호퍼',NULL,'1.000000000','묵직한 맛 산뜻함에 진한 달콤함을 더한 깔끔한 밸런스의 달콤 쌉쌀함 칵테일','1.000000000','2.404255319');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('10.2','1.520775623','BRANDY','2.200000000',NULL,'36','99','5','Horse’s Neck','호시스 넥',NULL,'4.000000000','묵직한 단맛 베이스에 생기 있는 새콤함과 짙은 여운이 어우러진 낮은 도수의 칵테일','1.775757576','3.344680851');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('10.3','1.531855956','VODKA','2.371428571',NULL,'37','68','5','Moscow Mule','모스코 뮬',NULL,'4.000000000','부담 없이 가벼운 진 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','2.648484848','2.531914894');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('12.2','1.742382271','TEQUILA','2.142857143',NULL,'38','62','5','Paloma','팔로마',NULL,'4.000000000','강렬한 새콤함 산뜻함에 진한 달콤함을 더한 산뜻하고 가벼운 묵직한 쓴맛 칵테일','2.939393939','2.680851064');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('22.6','2.894736842','GIN','1.377142857',NULL,'39','44','5','South Side','사우스 사이드',NULL,'1.000000000','화사한 달콤함 베이스에 톡 쏘는 신맛과 은근한 뒷맛이 어우러진 목 넘김이 편한 칵테일','2.454545455','1.723404255');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('9.7','1.465373961','WINE','3.571428571',NULL,'40','46','5','Spritz','스프리츠',NULL,'4.000000000','부담 없이 가벼운 와인 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','2.600000000','2.744680851');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('36.7','4.457063712','OTHER','1.514285714',NULL,'41','102','6','Angel Face','엔젤 페이스',NULL,'1.000000000','튀지 않는 산미 산뜻함에 기분 좋은 단맛을 더한 묵직한 고도수의 묵직한 쓴맛 칵테일','1.436363636','1.978723404');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('34.3','4.191135734','VODKA','1.457142857',NULL,'42','107','6','Black Russian','블랙 러시안',NULL,'1.000000000','은근한 당도 베이스에 차분함과 짙은 여운이 어우러진 도수 높고 진한 칵테일','1.000000000','1.468085106');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('36.4','4.423822715','GIN','1.514285714',NULL,'43','87','6','Dry Martini','드라이 마티니',NULL,'1.000000000','강렬한 타격감의 진 베이스로, 드라이함과 차분한 결이 조화를 이룬 깊은 쌉쌀함 칵테일','1.096969697','1.000000000');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('36.5','4.434903047','WHISKEY','2.371428571',NULL,'44','71','6','Manhattan','맨해튼',NULL,'1.000000000','묵직한 맛 산뜻함에 기분 좋은 단맛을 더한 묵직한 고도수의 묵직한 쓴맛 칵테일','1.096969697','1.600000000');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('32.0','3.936288089','WHISKEY','1.708571429',NULL,'45','76','6','Mint Julep','민트 주렙',NULL,'1.000000000','화사한 달콤함 베이스에 차분함과 짙은 여운이 어우러진 도수 높고 진한 칵테일','1.000000000','1.936170213');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('35.6','4.335180055','WHISKEY','1.628571429',NULL,'46','61','6','Old Fashioned','올드 패션드',NULL,'1.000000000','강렬한 타격감의 위스키 베이스로, 은은한 단맛과 차분한 결이 조화를 이룬 깊은 쌉쌀함 칵테일','1.000000000','1.710638298');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('40.0','4.822714681','WHISKEY','2.200000000',NULL,'47','49','6','Rusty Nail','러스티 네일',NULL,'1.000000000','묵직한 맛 산뜻함에 기분 좋은 단맛을 더한 묵직한 고도수의 묵직한 쓴맛 칵테일','1.000000000','1.787234043');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('35.7','4.346260388','BRANDY','1.685714286',NULL,'48','32','6','Stinger','스팅어',NULL,'1.000000000','화사한 달콤함 베이스에 차분함과 짙은 여운이 어우러진 도수 높고 진한 칵테일','1.242424242','1.978723404');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('22.0','2.828254848','LIQUEUR','2.885714286',NULL,'49','116','7','Chartreuse Swizzle','샤르트뢰즈 스위즐',NULL,'1.000000000','부드럽고 매끄러운 리큐르 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','2.672727273','2.712765957');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('21.7','2.795013850','VODKA','2.885714286',NULL,'50','88','7','Espresso Martini','에스프레소 마티니',NULL,'1.000000000','묵직한 맛 산뜻함에 진한 달콤함을 더한 깔끔한 밸런스의 묵직한 쓴맛 칵테일','1.290909091','2.404255319');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('16.6','2.229916898','RUM','3.142857143',NULL,'51','81','7','Jungle Bird','정글 버드',NULL,'1.000000000','묵직한 단맛 베이스에 톡 쏘는 신맛과 짙은 여운이 어우러진 목 넘김이 편한 칵테일','2.711515152','3.034042553');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('22.8','2.916897507','TEQUILA','2.931428571',NULL,'52','69','7','Naked and Famous','네이키드 앤 페이머스',NULL,'1.000000000','부드럽고 매끄러운 리큐르 베이스로, 은은한 단맛과 짜릿한 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','2.309090909','1.821276596');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('22.6','2.894736842','WHISKEY','2.285714286',NULL,'53','59','7','New York Sour','뉴욕 사워',NULL,'1.000000000','강렬한 새콤함 산뜻함에 진한 달콤함을 더한 깔끔한 밸런스의 묵직한 쓴맛 칵테일','2.600000000','2.489361702');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('21.5','2.772853186','WHISKEY','3.571428571',NULL,'54','63','7','Paper Plane','페이퍼 플레인',NULL,'1.000000000','묵직한 단맛 베이스에 톡 쏘는 신맛과 짙은 여운이 어우러진 목 넘김이 편한 칵테일','2.745454545','2.361702128');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('24.2','3.072022161','WHISKEY','2.280000000',NULL,'55','65','7','Penicillin','페니실린',NULL,'1.000000000','부드럽고 매끄러운 진 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','2.120000000','2.182978723');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('15.5','2.108033241','WINE','2.800000000',NULL,'56','41','7','Sherry Cobbler','셰리 코블러',NULL,'1.000000000','강렬한 새콤함 산뜻함에 진한 달콤함을 더한 깔끔한 밸런스의 묵직한 쓴맛 칵테일','2.008484848','2.170212766');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('13.3','1.864265928','LIQUEUR','3.057142857',NULL,'57','101','8','Americano','아메리카노',NULL,'3.000000000','화사한 달콤함 베이스에 차분함과 짙은 여운이 어우러진 낮은 도수의 칵테일','1.290909091','1.978723404');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('17.1','2.285318560','RUM','1.342857143',NULL,'58','113','8','Canchanchara','칸찬차라',NULL,'1.000000000','부드럽고 매끄러운 리큐르 베이스로, 은은한 단맛과 싱그러운 산미가 조화를 이룬 은은한 쌉싸름함 칵테일','1.727272727','1.851063830');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('16.0','2.163434903','GIN','1.514285714',NULL,'59','90','8','French 75','프렌치 75',NULL,'4.000000000','강렬한 새콤함 산뜻함에 기분 좋은 단맛을 더한 깔끔한 밸런스의 묵직한 쓴맛 칵테일','2.600000000','1.914893617');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('14.4','1.986149584','GIN','1.257142857',NULL,'60','94','8','Gin Fizz','진 피즈',NULL,'3.000000000','은근한 당도 베이스에 톡 쏘는 신맛과 은근한 뒷맛이 어우러진 낮은 도수의 칵테일','2.454545455','1.510638298');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('12.0','1.720221607','GIN','1.257142857',NULL,'61','80','8','John Collins','존 콜린스',NULL,'3.000000000','부담 없이 가벼운 진 베이스로, 은은한 단맛과 짜릿한 산미가 조화를 이룬 은은한 쌉싸름함 칵테일','2.454545455','1.723404255');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('12.8','1.808864266','WINE','1.571428571',NULL,'62','82','8','Kir','키르',NULL,'1.000000000','강렬한 새콤함 산뜻함에 진한 달콤함을 더한 산뜻하고 가벼운 묵직한 쓴맛 칵테일','2.406060606','2.021276596');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('13.3','1.864265928','RUM','1.291428571',NULL,'63','66','8','Mojito','모히토',NULL,'3.000000000','화사한 달콤함 베이스에 생기 있는 새콤함과 은근한 뒷맛이 어우러진 낮은 도수의 칵테일','1.969696970','1.659574468');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('18.2','2.407202216','OTHER','2.314285714',NULL,'64','33','8','Suffering Bastard','서퍼링 바스타드',NULL,'3.000000000','부드럽고 매끄러운 진 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','2.454545455','2.051063830');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('11.8','1.698060942','VODKA','1.554285714',NULL,'65','108','9','Bloody Mary','블러디 메리',NULL,'1.000000000','강렬한 새콤함 산뜻함에 절제된 단맛을 더한 산뜻하고 가벼운 묵직한 쓴맛 칵테일','3.070303030','1.412765957');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('20.0','2.606648199','VODKA','1.085714286',NULL,'66','119','9','Cosmopolitan','코스모폴리탄',NULL,'1.000000000','화사한 달콤함 베이스에 톡 쏘는 신맛과 깨끗한 끝맛이 어우러진 목 넘김이 편한 칵테일','2.575757576','1.851063830');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('23.9','3.038781163','TEQUILA','1.828571429',NULL,'67','78','9','Illegal','일리걸',NULL,'1.000000000','부드럽고 매끄러운 럼 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','2.406060606','2.268085106');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('24.7','3.127423823','RUM','1.514285714',NULL,'68','70','9','Mai-Tai','마이타이',NULL,'1.000000000','강렬한 새콤함 산뜻함에 진한 달콤함을 더한 깔끔한 밸런스의 묵직한 쓴맛 칵테일','2.672727273','2.744680851');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('14.3','1.975069252','VODKA','1.000000000',NULL,'69','40','9','Sex on the Beach','섹스 온 더 비치',NULL,'1.000000000','묵직한 단맛 베이스에 톡 쏘는 신맛과 깨끗한 끝맛이 어우러진 낮은 도수의 칵테일','2.648484848','2.744680851');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('11.1','1.620498615','GIN','1.531428571',NULL,'70','43','9','Singapore Sling','싱가포르 슬링',NULL,'1.000000000','부담 없이 가벼운 진 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','4.214545455','4.217021277');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('11.4','1.653739612','TEQUILA','1.514285714',NULL,'71','34','9','Tequila Sunrise','테킬라 선라이즈',NULL,'1.000000000','강렬한 새콤함 산뜻함에 진한 달콤함을 더한 산뜻하고 가벼운 묵직한 쓴맛 칵테일','3.036363636','3.063829787');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('24.8','3.138504155','RUM','1.800000000',NULL,'72','35','9','Three Dots and a Dash','쓰리 닷츠 앤 어 대시',NULL,'1.000000000','묵직한 단맛 베이스에 톡 쏘는 신맛과 짙은 여운이 어우러진 목 넘김이 편한 칵테일','2.236363636','2.370212766');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('30.0','3.714681440','OTHER','1.514285714',NULL,'73','106','10','Between the Sheets','비트윈 더 시츠',NULL,'1.000000000','강렬한 타격감의 브랜디 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','2.260606061','2.319148936');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('23.7','3.016620499','GIN','1.257142857',NULL,'74','117','10','Clover Club','클로버 클럽',NULL,'1.000000000','상큼한 풍미 산뜻함에 절제된 단맛을 더한 깔끔한 밸런스의 달콤 쌉쌀함 칵테일','1.872727273','1.531914894');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('9.8','1.476454294','LIQUEUR','4.428571429',NULL,'75','89','10','Fernandito','페르난디토',NULL,'5.000000000','묵직한 단맛 베이스에 생기 있는 새콤함과 짙은 여운이 어우러진 낮은 도수의 칵테일','1.727272727','5.000000000');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('34.0','4.157894737','BRANDY','1.800000000',NULL,'76','91','10','French Connection','프렌치 커넥션',NULL,'1.000000000','강렬한 타격감의 브랜디 베이스로, 녹진한 단맛과 차분한 결이 조화를 이룬 깊은 쌉쌀함 칵테일','1.169696970','2.446808511');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('27.6','3.448753463','VODKA','1.085714286',NULL,'77','92','10','French Martini','프렌치 마티니',NULL,'1.000000000','튀지 않는 산미에 기분 좋은 단맛을 더한 칵테일','1.436363636','1.659574468');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('27.7','3.459833795','GIN','1.285714286',NULL,'78','64','10','Paradise','파라다이스',NULL,'1.000000000','화사한 달콤함 베이스에 부드러운 산뜻함과 은근한 뒷맛이 어우러진 술맛이 확실한 칵테일','1.387878788','1.723404255');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('41.6','5.000000000','BRANDY','2.074285714',NULL,'79','50','10','Sazerac','사제락',NULL,'1.000000000','강렬한 타격감의 브랜디 베이스로, 은은한 단맛과 차분한 결이 조화를 이룬 깊은 쌉쌀함 칵테일','1.242424242','1.612765957');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('37.4','4.534626039','GIN','1.342857143',NULL,'80','29','10','Vesper','베스퍼',NULL,'1.000000000','묵직한 맛 산뜻함에 깔끔함을 더한 묵직한 고도수의 달콤 쌉쌀함 칵테일','1.038787879','1.055319149');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('20.0','2.606648199','RUM','1.257142857',NULL,'81','86','11','Don’s Special Daiquiri','돈스 스페셜 다이키리',NULL,'1.000000000','묵직한 단맛 베이스에 생기 있는 새콤함과 은근한 뒷맛이 어우러진 목 넘김이 편한 칵테일','1.945454545','2.361702128');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('22.6','2.894736842','GIN','1.348571429',NULL,'82','93','11','Gin Basil Smash','진 바질 스매시',NULL,'1.000000000','부드럽고 매끄러운 진 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 은은한 쌉싸름함 칵테일','2.095757576','2.010638298');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('19.9','2.595567867','RUM','1.085714286',NULL,'83','74','11','Mary Pickford','메리 픽포드',NULL,'1.000000000','상큼한 풍미 산뜻함에 진한 달콤함을 더한 깔끔한 밸런스의 투명한 뒷맛 칵테일','1.935757576','2.297872340');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('23.5','2.994459834','GIN','1.857142857',NULL,'84','67','11','Monkey Gland','몽키 글랜드',NULL,'1.000000000','묵직한 단맛 베이스에 생기 있는 새콤함과 짙은 여운이 어우러진 목 넘김이 편한 칵테일','1.945454545','2.361702128');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('21.5','2.772853186','RUM','1.514285714',NULL,'85','54','11','Planters Punch','플랜터스 펀치',NULL,'1.000000000','부드럽고 매끄러운 럼 베이스로, 녹진한 단맛과 싱그러운 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','1.727272727','2.872340426');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('21.4','2.761772853','WINE','1.600000000',NULL,'86','56','11','Porto Flip','포토 플립',NULL,'1.000000000','튀지 않는 산미 산뜻함에 진한 달콤함을 더한 깔끔한 밸런스의 묵직한 쓴맛 칵테일','1.509090909','2.276595745');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('19.4','2.540166205','VODKA','1.011428571',NULL,'87','45','11','Spicy Fifty','스파이시 피프티',NULL,'1.000000000','묵직한 단맛 베이스에 생기 있는 새콤함과 깨끗한 끝맛이 어우러진 목 넘김이 편한 칵테일','1.800000000','2.212765957');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('19.8','2.584487535','WHISKEY','1.514285714',NULL,'88','31','11','Whiskey Sour','위스키 사워',NULL,'1.000000000','부드럽고 매끄러운 위스키 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','2.212121212','2.489361702');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('21.7','2.795013850','BRANDY','1.857142857',NULL,'89','100','12','Alexander','알렉산더',NULL,'1.000000000','묵직한 맛 산뜻함에 진한 달콤함을 더한 깔끔한 밸런스의 묵직한 쓴맛 칵테일','1.145454545','2.489361702');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('30.7','3.792243767','GIN','2.028571429',NULL,'90','114','12','Cardinale','카르디날레',NULL,'1.000000000','담백함 베이스에 차분함과 짙은 여운이 어우러진 도수 높고 진한 칵테일','1.242424242','1.170212766');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('32.3','3.969529086','TEQUILA','1.857142857',NULL,'91','95','12','Grand Margarita','그랑 마가리타',NULL,'1.000000000','강렬한 타격감의 데킬라 베이스로, 은은한 단맛과 짜릿한 산미가 조화를 이룬 깊은 칵테일','2.090909091','1.914893617');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('8.9','1.376731302','WHISKEY','5.000000000',NULL,'92','79','12','Irish Coffee','아이리시 커피',NULL,'1.000000000','튀지 않는 산미 산뜻함에 진한 달콤함을 더한 산뜻하고 가벼운 묵직한 쓴맛 칵테일','1.581818182','2.531914894');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('31.8','3.914127424','GIN','2.285714286',NULL,'93','83','12','Last Word','라스트 워드',NULL,'1.000000000','화사한 달콤함 베이스에 톡 쏘는 신맛과 짙은 여운이 어우러진 도수 높고 진한 칵테일','2.202424242','1.914893617');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('23.9','3.038781163','LIQUEUR','4.000000000',NULL,'94','38','12','Trinidad Sour','트리니다드 사워',NULL,'1.000000000','부드럽고 매끄러운 위스키 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 깊은 쌉쌀함 칵테일','2.090909091','2.459574468');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('28.5','3.548476454','GIN','1.920000000',NULL,'95','39','12','Tuxedo','턱시도',NULL,'1.000000000','튀지 않는 산미 산뜻함에 절제된 단맛을 더한 적당한 볼륨감의 묵직한 쓴맛 칵테일','1.320000000','1.421276596');
INSERT INTO `cocktails` (`alcohol_degree`,`alcohol_intensity`,`base_spirit`,`bitterness`,`description`,`id`,`image_id`,`mood_type_id`,`name_en`,`name_ko`,`pairing_snack`,`refreshing`,`short_description`,`sourness`,`sweetness`) VALUES ('30.4','3.759002770','RUM','2.382857143',NULL,'96','27','12','Zombie','좀비',NULL,'1.000000000','묵직한 단맛 베이스에 톡 쏘는 신맛과 짙은 여운이 어우러진 도수 높고 진한 칵테일','2.212121212','2.838297872');

-- cocktail_ingredients: 383 reference rows
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','1','383','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','1','384','마라스키노 룩사르도','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','1','385','신선한 레몬 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 bsp','1','386','크렘 드 바이올렛','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('52.5 ml','2','387','브랜디','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('7.5 ml','2','388','마라스키노 룩사르도','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 bsp','2','389','큐라소','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','2','390','신선한 레몬 주스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 bsp','2','391','심플 시럽','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 dash','2','392','아로마틱 비터스','6');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('40 ml','3','393','올드 톰 진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 ml','3','394','마라스키노 룩사르도','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 ml','3','395','신선한 레몬 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 dash','3','396','오렌지 비터스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','4','397','화이트 쿠바 럼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','4','398','신선한 라임 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 bsp','4','399','고운 설탕','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','5','400','보드카','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','5','401','트리플 섹','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','5','402','갓 짠 레몬 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','6','403','데킬라','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','6','404','트리플 섹','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','6','405','갓 짠 라임 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','7','406','코냑','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','7','407','트리플 섹','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','7','408','신선한 레몬 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('40 ml','8','409','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','8','410','트리플 섹','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','8','411','신선한 레몬 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','9','412','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','9','413','코인트루','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','9','414','릴레 블랑','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','9','415','신선한 레몬 주스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 dash','9','416','압생트','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','10','417','보드카','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','10','418','데킬라','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','10','419','화이트 럼','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','10','420','진','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','10','421','코인트루','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('25 ml','10','422','레몬 주스','6');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','10','423','심플 시럽','7');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','10','424','콜라','8');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('75 ml','11','425','갓 짠 오렌지 주스','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('75 ml','11','426','프로세코','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','12','427','화이트 럼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','12','428','피치 브랜디','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','12','429','신선한 라임 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','12','430','꿀 믹스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 장','12','431','민트 잎','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('3~4 개','12','432','파인애플 조각','6');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('6~8 장','13','433','민트 잎','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','13','434','숙성 럼','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','13','435','신선한 라임 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','13','436','심플 시럽','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 dash','13','437','앙고스투라 비터스','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','13','438','브뤼 샴페인 또는 프로세코','6');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','14','439','화이트 럼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','14','440','코코넛 크림','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','14','441','신선한 파인애플 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','15','442','바닐라 보드카','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','15','443','패션후르츠 리큐르','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','15','444','패션후르츠 퓨레','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 bsp','15','445','바닐라 설탕','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','15','446','사이드용 샴페인','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('25 ml','16','447','보드카','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('25 ml','16','448','신선한 레몬 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','16','449','크렘 드 카시스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 ml','16','450','설탕 시럽','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('채우기','16','451','스파클링 와인','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('52.5 ml','17','452','드라이 진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 tsp','17','453','꿀 시럽','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','17','454','신선한 레몬 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','17','455','신선한 오렌지 주스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','18','456','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('25 ml','18','457','신선한 레몬 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('12.5 ml','18','458','설탕 시럽','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','18','459','크렘 드 뮤르 (블랙베리 리큐르)','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','19','460','카샤사','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 개','19','461','작은 조각으로 썬 라임','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('4 tsp','19','462','사탕수수 설탕','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','20','463','럼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('40 ml','20','464','자몽 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','20','465','마라스키노 룩사르도','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','20','466','신선한 라임 주스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','21','467','피스코','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','21','468','신선한 파인애플 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','21','469','심플 시럽','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','21','470','신선한 레몬 주스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','21','471','드라이 화이트 와인','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('3 개','21','472','정향','6');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','22','473','피스코','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','22','474','신선한 레몬 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','22','475','심플 시럽','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1개 분량','22','476','생달걀 흰자','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','23','477','데킬라','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','23','478','신선한 라임 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','23','479','아가베 넥타','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','24','480','부드러운 화이트 그라파','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','24','481','신선한 레몬 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','24','482','꿀 믹스 (물 대신 캐모마일 티 사용)','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','24','483','캐모마일 코디얼','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('몇 방울','24','484','달걀 흰자','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','25','485','버번 또는 라이 위스키','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','25','486','비터 캄파리','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','25','487','스위트 레드 베르무트','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','26','488','런던 드라이 진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','26','489','스위트 레드 베르무트','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('7.5 ml','26','490','페르넷 브랑카','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','27','491','런던 드라이 진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','27','492','스위트 레드 베르무트','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 bsp','27','493','마라스키노 룩사르도','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 dash','27','494','오렌지 비터스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','28','495','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','28','496','비터 캄파리','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','28','497','스위트 레드 베르무트','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','29','498','카샤사','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','29','499','스위트 베르무트 (친자노 로쏘)','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','29','500','치나르','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 방울','29','501','앙고스투라 비터스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','30','502','라이 위스키','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','30','503','스위트 베르무트','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','30','504','체리 브랜디 룩사르도','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('7.5 ml','30','505','압생트','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','31','506','아이리시 위스키','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('25 ml','31','507','스위트 레드 베르무트','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','31','508','그린 샤르트뢰즈','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 dash','31','509','앙고스투라 비터스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','32','510','라이 위스키','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','32','511','코냑','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','32','512','스위트 베르무트','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 bsp','32','513','베네딕틴','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 dash','32','514','페쇼 비터스','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('100 ml','33','515','프로세코','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','33','516','백도 퓨레','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','34','517','고슬링 럼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('100 ml','34','518','진저 비어','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','35','519','크렘 드 카카오 (화이트)','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','35','520','크렘 드 망트 (그린)','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','35','521','신선한 크림','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('40 ml','36','522','코냑','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('120 ml','36','523','진저에일','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 dash (선택)','36','524','앙고스투라 비터스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','37','525','스미노프 보드카','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('120 ml','37','526','진저 비어','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 ml','37','527','신선한 라임 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','38','528','데킬라','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('5 ml','38','529','신선한 라임 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('한 꼬집','38','530','소금','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('100 ml','38','531','핑크 자몽 소다','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','39','532','런던 드라이 진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','39','533','신선한 레몬 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','39','534','심플 시럽','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('5~6 장','39','535','민트 잎','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('몇 방울 (선택)','39','536','달걀 흰자','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('90 ml','40','537','프로세코','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','40','538','아페롤','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('약간','40','539','소다수','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','41','540','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','41','541','앱리코트(살구) 브랜디','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','41','542','칼바도스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','42','543','보드카','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','42','544','커피 리큐르','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','43','545','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 ml','43','546','드라이 베르무트','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','44','547','라이 위스키','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','44','548','스위트 레드 베르무트','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 dash','44','549','앙고스투라 비터스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','45','550','버번 위스키','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('4 개','45','551','신선한 민트 줄기','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 tsp','45','552','고운 설탕','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 tsp','45','553','물','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','46','554','버번 또는 라이 위스키','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 개','46','555','각설탕','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('몇 dash','46','556','앙고스투라 비터스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('몇 방울','46','557','일반 물','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','47','558','스카치 위스키','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('25 ml','47','559','드람뷔','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','48','560','코냑','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','48','561','화이트 크렘 드 망트 (민트 리큐르)','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','49','562','그린 샤르트뢰즈','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','49','563','신선한 파인애플 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','49','564','신선한 라임 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','49','565','팔레넘','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','50','566','보드카','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','50','567','깔루아','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 ml','50','568','설탕 시럽','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 샷','50','569','진한 에스프레소','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','51','570','블랙스트랩 럼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','51','571','캄파리','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','51','572','파인애플 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','51','573','갓 짠 라임 주스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','51','574','데메라라 설탕 시럽','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','52','575','메즈칼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','52','576','옐로우 샤르트뢰즈','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','52','577','아페롤','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','52','578','신선한 라임 주스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','53','579','라이 위스키 또는 버번','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','53','580','심플 시럽','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','53','581','신선한 레몬 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('몇 방울','53','582','달걀 흰자','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','53','583','레드 와인','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','54','584','버번 위스키','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','54','585','아마로 노니노','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','54','586','아페롤','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','54','587','신선한 레몬 주스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','55','588','블렌디드 스카치 위스키','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('7.5 ml','55','589','라가불린 16년(아일라 싱글몰트)','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','55','590','신선한 레몬 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','55','591','꿀 시럽','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2~3 조각','55','592','동전 크기 신선한 생강 슬라이스','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','56','593','아몬티야도 셰리','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','56','594','팔로 코르타도','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 tsp','56','595','고운 설탕','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1/2 개','56','596','오렌지 슬라이스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1/2 개','56','597','레몬 슬라이스','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','57','598','비터 캄파리','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','57','599','스위트 레드 베르무트','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('약간','57','600','소다수','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','58','601','쿠바 아구아르디엔테 (또는 화이트 럼)','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','58','602','신선한 라임 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','58','603','생꿀','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','58','604','물','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','59','605','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','59','606','신선한 레몬 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','59','607','설탕 시럽','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','59','608','샴페인','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','60','609','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','60','610','신선한 레몬 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 ml','60','611','심플 시럽','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('약간 (약 40ml)','60','612','소다수','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','61','613','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','61','614','신선한 레몬 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','61','615','심플 시럽','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','61','616','소다수','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('90 ml','62','617','드라이 화이트 와인','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 ml','62','618','크렘 드 카시스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','63','619','화이트 쿠바 럼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','63','620','신선한 라임 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('6 개','63','621','민트 줄기','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 tsp','63','622','사탕수수 설탕','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('약간','63','623','소다수','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','64','624','코냑 또는 브랜디','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','64','625','진','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','64','626','신선한 라임 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 dash','64','627','앙고스투라 비터스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('채우기','64','628','진저 비어','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','65','629','보드카','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('90 ml','65','630','토마토 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','65','631','신선한 레몬 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 dash','65','632','우스터소스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('취향껏','65','633','타바스코/셀러리 소금/후추','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('40 ml','66','634','시트론 보드카','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','66','635','코인트루','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','66','636','신선한 라임 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','66','637','크랜베리 주스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','67','638','에스파딘 메즈칼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','67','639','자메이카 오버프루프 화이트 럼','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','67','640','팔레넘','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 bsp','67','641','마라스키노 룩사르도','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','67','642','신선한 라임 주스','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','67','643','심플 시럽','6');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('몇 방울 (선택)','67','644','달걀 흰자','7');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','68','645','자메이카 앰버 럼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','68','646','마르티니크 몰라세스 럼','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','68','647','오렌지 큐라소','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','68','648','오르제(아몬드) 시럽','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','68','649','갓 짠 라임 주스','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('7.5 ml','68','650','심플 시럽','6');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('40 ml','69','651','보드카','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','69','652','피치 스냅스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('40 ml','69','653','신선한 오렌지 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('40 ml','69','654','크랜베리 주스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','70','655','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','70','656','체리 브랜디(상그 드 몰라코)','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('7.5 ml','70','657','코인트루','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('7.5 ml','70','658','DOM 베네딕틴','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('120 ml','70','659','신선한 파인애플 주스','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','70','660','신선한 라임 주스','6');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 ml','70','661','그레나딘 시럽','7');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 dash','70','662','앙고스투라 비터스','8');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','71','663','데킬라','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('90 ml','71','664','신선한 오렌지 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','71','665','그레나딘 시럽','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','72','666','마르티니크 아그리콜 럼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','72','667','블렌디드 숙성 럼','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('7.5 ml','72','668','팔레넘','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('7.5 ml','72','669','올스파이스 릭큐르','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','72','670','신선한 라임 주스','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','72','671','신선한 오렌지 주스','6');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','72','672','꿀 시럽','7');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 dash','72','673','앙고스투라 비터스','8');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','73','674','화이트 럼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','73','675','코냑','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','73','676','트리플 섹','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','73','677','신선한 레몬 주스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','74','678','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','74','679','라즈베리 시럽','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','74','680','신선한 레몬 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('몇 방울','74','681','달걀 흰자','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','75','682','페르넷 브랑카','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('약 200 ml (가득 채우기)','75','683','콜라','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('35 ml','76','684','코냑','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('35 ml','76','685','아마레토','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','77','686','보드카','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','77','687','라즈베리 리큐르','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','77','688','신선한 파인애플 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','78','689','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','78','690','앱리코트(살구) 브랜디','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','78','691','신선한 오렌지 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','79','692','코냑','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 ml','79','693','압생트','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 개','79','694','각설탕','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 dash','79','695','페쇼 비터스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','80','696','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','80','697','보드카','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('7.5 ml','80','698','릴레 블랑','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','81','699','골드 자메이카 럼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','81','700','쿠바 럼','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','81','701','패션후르츠 시럽','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','81','702','신선한 라임 주스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','81','703','꿀 시럽','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('60 ml','82','704','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','82','705','갓 짠 레몬 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','82','706','설탕 시럽','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 장','82','707','이탈리안 바질 잎','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','83','708','화이트 럼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','83','709','신선한 파인애플 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('7.5 ml','83','710','마라스키노 룩사르도','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('5 ml','83','711','그레나딘 시럽','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','84','712','드라이 진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','84','713','신선한 오렌지 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 tbsp','84','714','압생트','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 tbsp','84','715','그레나딘 시럽','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','85','716','자메이카 럼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','85','717','라임 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','85','718','사탕수수 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','86','719','브랜디','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','86','720','레드 타우니 포트 와인','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 ml','86','721','달걀 노른자','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','87','722','바닐라 보드카','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','87','723','엘더플라워 코디얼','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','87','724','신선한 라임 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 ml','87','725','모닌 꿀 시럽','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('2 조각','87','726','얇게 썬 홍고추','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','88','727','버번 위스키','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('25 ml','88','728','신선한 레몬 주스','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','88','729','설탕 시럽','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('몇 방울 (선택)','88','730','달걀 흰자','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','89','731','코냑','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','89','732','크렘 드 카카오 (브라운)','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','89','733','신선한 크림','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('40 ml','90','734','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','90','735','드라이 베르무트','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('10 ml','90','736','비터 캄파리','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','91','737','데킬라 (100% 아가베)','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','91','738','그랑 마니에르','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','91','739','신선한 라임 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','92','740','아이리시 위스키','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('120 ml','92','741','뜨거운 커피','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('50 ml','92','742','신선한 크림','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 tsp','92','743','설탕','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','93','744','진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','93','745','그린 샤르트뢰즈','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','93','746','마라스키노 룩사르도','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','93','747','신선한 라임 주스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','94','748','앙고스투라 비터스','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','94','749','오르제 시럽','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('22.5 ml','94','750','신선한 레몬 주스','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','94','751','라이 위스키','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','95','752','올드 톰 진','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','95','753','드라이 베르무트','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1/2 bsp','95','754','마라스키노 룩사르도','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1/4 bsp','95','755','압생트','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('3 dash','95','756','오렌지 비터스','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','96','757','자메이카 다크 럼','1');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('45 ml','96','758','골드 푸에르토리코 럼','2');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('30 ml','96','759','데메라라 럼','3');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('20 ml','96','760','신선한 라임 주스','4');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','96','761','팔레넘','5');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('15 ml','96','762','돈스 믹스','6');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 tsp','96','763','그레나딘 시럽','7');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('1 dash','96','764','앙고스투라 비터스','8');
INSERT INTO `cocktail_ingredients` (`amount_text`,`cocktail_id`,`id`,`name`,`sort_order`) VALUES ('6 방울','96','765','페르노','9');

-- cocktail_recipe_steps: 288 reference rows
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('1','칵테일 셰이커에 모든 재료를 넣어주세요.','191','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('1','얼음을 채우고 시원해질 때까지 잘 흔들어주세요.','192','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('1','차갑게 식힌 칵테일 글라스에 얼음을 걸러서 따라주세요.','193','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('2','슬림 칵테일 글라스 가장자리에 레몬즙을 바르고 설탕을 묻혀서 준비해주세요.','194','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('2','믹싱 글라스에 얼음과 모든 재료를 넣어주세요.','195','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('2','바스푼으로 잘 저어준 뒤 준비한 잔에 걸러서 따라주세요.','196','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('3','칵테일 셰이커에 모든 재료를 넣어주세요.','197','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('3','얼음을 넣고 재료가 잘 섞이도록 흔들어주세요.','198','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('3','얼음을 채운 차가운 락 글라스에 거르고 오렌지 필과 체리로 장식해주세요.','199','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('4','칵테일 셰이커에 럼, 라임 주스, 설탕을 넣어주세요.','200','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('4','설탕이 완전히 녹을 때까지 바스푼으로 잘 저어주세요.','201','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('4','얼음을 넣고 흔든 뒤 차가운 칵테일 글라스에 걸러서 따라주세요.','202','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('5','셰이커에 보드카, 트리플 섹, 레몬 주스를 넣어주세요.','203','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('5','얼음을 가득 채우고 차가워질 때까지 잘 흔들어주세요.','204','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('5','차갑게 식힌 칵테일 글라스에 부드럽게 걸러서 따라주세요.','205','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('6','칵테일 글라스 가장자리에 라임 즙을 바르고 소금을 묻혀주세요.','206','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('6','셰이커에 데킬라, 트리플 섹, 라임 주스와 얼음을 넣어주세요.','207','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('6','잘 흔든 후 준비한 잔에 얼음을 걸러서 따라주세요.','208','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('7','칵테일 셰이커에 코냑, 트리플 섹, 레몬 주스를 넣어주세요.','209','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('7','얼음을 채우고 온도가 낮아질 때까지 힘차게 흔들어주세요.','210','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('7','차갑게 식힌 칵테일 글라스에 걸러서 따라주세요.','211','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('8','셰이커에 진, 트리플 섹, 레몬 주스를 넣어주세요.','212','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('8','얼음을 가득 넣고 재료가 혼합되도록 잘 흔들어주세요.','213','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('8','차갑게 식힌 칵테일 글라스에 얼음을 거르고 따라주세요.','214','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('9','셰이커에 진, 코인트루, 릴레 블랑, 레몬 주스, 압생트를 넣어주세요.','215','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('9','얼음을 넣고 시원해질 때까지 잘 흔들어주세요.','216','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('9','차갑게 식힌 칵테일 글라스에 걸러서 완성해주세요.','217','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('10','하이볼 글라스에 얼음을 가득 채워주세요.','218','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('10','콜라를 제외한 모든 재료를 잔에 차례대로 따라주세요.','219','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('10','남은 공간을 콜라로 가득 채운 뒤 바스푼으로 가볍게 저어주세요.','220','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('11','플루트 글라스(샴페인 잔)에 신선한 오렌지 주스를 먼저 따라주세요.','221','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('11','그 위에 프로세코 스파클링 와인을 천천히 부어주세요.','222','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('11','층이 섞이도록 바스푼으로 가볍게 한두 번 저어주세요.','223','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('12','블렌더에 럼, 피치 브랜디, 라임 주스, 꿀 믹스, 민트 잎, 파인애플을 넣어주세요.','224','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('12','잘게 부순 얼음 반 컵을 넣고 부드럽게 갈아주세요.','225','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('12','그란데 글라스에 예쁘게 담아 완성해주세요.','226','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('13','셰이커에 민트 잎을 넣고 가볍게 으깬 뒤 럼, 라임 주스, 시럽, 비터스를 넣어주세요.','227','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('13','얼음을 채우고 잘 흔든 뒤 차가운 글라스에 걸러서 따라주세요.','228','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('13','마지막으로 스파클링 와인을 위에 채워주세요.','229','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('14','전기 블렌더에 럼, 코코넛 크림, 파인애플 주스를 넣어주세요.','230','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('14','얼음을 함께 넣고 살얼음이 생길 때까지 부드럽게 갈아주세요.','231','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('14','큰 글라스에 부어준 뒤 빨대를 꽂아 완성해주세요.','232','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('15','셰이커에 바닐라 보드카, 패션후르츠 리큐르, 퓨레, 설탕을 넣어주세요.','233','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('15','얼음과 함께 흔든 뒤 차가운 글라스에 이중으로 걸러서 따라주세요.','234','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('15','따로 준비한 차가운 샴페인 한 샷을 곁들여주세요.','235','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('16','하이볼 글라스에 보드카, 레몬 주스, 카시스, 설탕 시럽을 넣어주세요.','236','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('16','얼음을 채우고 재료가 섞이도록 가볍게 저어주세요.','237','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('16','남은 공간을 스파클링 와인으로 가득 채워주세요.','238','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('17','셰이커에 드라이 진, 꿀 시럽, 레몬 주스, 오렌지 주스를 넣어주세요.','239','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('17','얼음을 가득 채우고 차가워질 때까지 잘 흔들어주세요.','240','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('17','식혀둔 칵테일 글라스에 얼음을 거르고 따라주세요.','241','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('18','잔에 진, 레몬 주스, 설탕 시럽을 넣고 부순 얼음을 가득 채운 뒤 저어주세요.','242','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('18','크렘 드 뮤르(블랙베리 리큐르)를 얼음 위에 원을 그리며 천천히 부어주세요.','243','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('18','예쁜 번짐 효과가 생기면 완성입니다.','244','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('19','올드 패션드 글라스 바닥에 조각 낸 라임과 설탕을 넣어주세요.','245','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('19','머들러로 라임 즙이 나오도록 꾹꾹 으깨주세요.','246','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('19','부순 얼음을 가득 채운 뒤 카샤사를 붓고 가볍게 저어주세요.','247','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('20','칵테일 셰이커에 럼, 자몽 주스, 마라스키노 리큐르, 라임 주스를 넣어주세요.','248','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('20','얼음을 채우고 재료가 시원해지도록 잘 흔들어주세요.','249','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('20','차가운 칵테일 글라스에 거른 뒤 서빙해주세요.','250','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('21','셰이커에 와인을 제외한 피스코, 파인애플 주스, 시럽, 레몬 주스, 정향을 넣어주세요.','251','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('21','얼음과 함께 흔든 뒤 잔에 거른다.','252','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('21','마지막으로 드라이 화이트 와인을 부어 완성해주세요.','253','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('22','셰이커에 피스코, 레몬 주스, 시럽, 달걀 흰자를 넣고 얼음 없이 먼저 세게 흔들어 거품을 내주세요.','254','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('22','얼음을 추가하고 한 번 더 강하게 흔들어주세요.','255','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('22','차가운 잔에 부드럽게 걸러서 따라주세요.','256','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('23','셰이커에 데킬라, 신선한 라임 주스, 아가베 넥타를 넣어주세요.','257','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('23','얼음을 넣고 온도가 차가워질 때까지 잘 흔들어주세요.','258','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('23','얼음이 채워진 차가운 락 글라스에 거른 뒤 따라주세요.','259','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('24','셰이커에 그라파, 레몬 주스, 캐모마일 꿀 믹스, 캐모마일 코디얼, 달걀 흰자를 넣어주세요.','260','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('24','얼음을 가득 채우고 힘차게 흔들어 거품을 내주세요.','261','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('24','얼음이 담긴 잔에 부드럽게 걸러 따라주세요.','262','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('25','믹싱 글라스에 버번 위스키, 캄파리, 스위트 베르무트를 넣어주세요.','263','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('25','얼음을 가득 채우고 바스푼으로 결을 따라 부드럽게 저어주세요.','264','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('25','차갑게 식힌 칵테일 글라스에 얼음을 거르고 따라주세요.','265','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('26','믹싱 글라스에 드라이 진, 스위트 베르무트, 페르넷 브랑카를 넣어주세요.','266','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('26','얼음을 가득 채우고 차가워질 때까지 부드럽게 저어주세요.','267','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('26','차갑게 식힌 칵테일 글라스에 걸러서 따라주세요.','268','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('27','믹싱 글라스에 진, 스위트 베르무트, 마라스키노, 오렌지 비터스를 넣어주세요.','269','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('27','얼음을 채우고 재료가 희석되도록 바스푼으로 잘 저어주세요.','270','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('27','차가운 칵테일 글라스에 거른 뒤 완성해주세요.','271','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('28','올드 패션드 글라스에 커다란 얼음을 채워주세요.','272','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('28','진, 캄파리, 스위트 RED 베르무트를 정량대로 잔에 바로 부어주세요.','273','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('28','바스푼을 이용해 재료가 섞이도록 부드럽게 저어주세요.','274','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('29','믹싱 글라스에 카샤사, 스위트 베르무트, 치나르, 비터스를 넣어주세요.','275','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('29','얼음을 가득 채우고 차가워질 때까지 저어주세요.','276','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('29','얼음이 채워진 올드 패션드 글라스에 걸러서 따라주세요.','277','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('30','믹싱 글라스에 라이 위스키, 스위트 베르무트, 체리 브랜디, 압생트를 넣어주세요.','278','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('30','얼음을 넣고 재료가 칠링될 때까지 잘 저어주세요.','279','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('30','식혀둔 칵테일 잔에 깔끔하게 걸러 따라주세요.','280','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('31','믹싱 글라스에 아이리시 위스키, 스위트 베르무트, 그린 샤르트뢰즈, 비터스를 넣어주세요.','281','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('31','얼음을 넣고 온도가 내려갈 때까지 부드럽게 저어주세요.','282','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('31','차가운 글라스에 걸러 따라주세요.','283','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('32','믹싱 글라스에 위스키, 코냑, 스위트 베르무트, 베네딕틴, 비터스를 넣어주세요.','284','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('32','얼음과 함께 바스푼으로 부드럽게 저어주세요.','285','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('32','얼음이 담긴 올드 패션드 글라스에 걸러서 따라주세요.','286','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('33','플루트 글라스에 차가운 백도(화이트 피치) 퓨레를 먼저 채워주세요.','287','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('33','그 위에 프로세코 스파클링 와인을 천천히 부어주세요.','288','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('33','퓨레와 와인이 부드럽게 섞이도록 바스푼으로 살짝 저어주세요.','289','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('34','하이볼 글라스에 얼음을 가득 채워주세요.','290','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('34','진저 비어를 잔의 70% 정도 먼저 부어주세요.','291','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('34','그 위에 고슬링 다크 럼을 천천히 띄우듯(플로팅) 부어 예쁜 층을 만들어주세요.','292','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('35','칵테일 셰이커에 화이트 카카오, 그린 민트 리큐르, 신선한 크림을 넣어주세요.','293','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('35','얼음을 가득 채우고 크림이 부드럽게 섞이도록 힘차게 흔들어주세요.','294','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('35','차가운 칵테일 잔에 거른 뒤 완성해주세요.','295','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('36','하이볼 글라스에 길게 깎은 레몬 필로 장식하고 얼음을 채워두세요.','296','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('36','코냑과 진저에일을 비율에 맞게 잔에 바로 부어주세요.','297','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('36','탄산이 날아가지 않게 바스푼으로 가볍게 한 번 저어주세요.','298','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('37','동잔(뮬 컵)에 얼음을 가득 채워주세요.','299','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('37','보드카와 진저 비어를 잔에 바로 채워주세요.','300','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('37','신선한 라임 주스를 더한 뒤 재료가 섞이도록 가볍게 한 바퀴 저어주세요.','301','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('38','하이볼 글라스에 데킬라를 부어주세요.','302','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('38','신선한 라임 주스를 짜 넣고, 얼음과 소금 한 꼬집을 더해주세요.','303','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('38','남은 공간을 핑크 자몽 소다로 가득 채운 뒤 가볍게 저어주세요.','304','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('39','셰이커에 민트 잎을 넣고 가볍게 누른 뒤 진, 레몬 주스, 시럽, 달걀 흰자를 넣어주세요.','305','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('39','얼음을 넣고 부드러운 거품이 날 때까지 힘차게 흔들어주세요.','306','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('39','차가운 잔에 이중으로 걸러 따라주세요.','307','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('40','얼음을 가득 채운 와인 글라스에 프로세코 와인과 아페롤을 차례로 부어주세요.','308','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('40','소다수를 살짝 더해 청량감을 더해주세요.','309','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('40','탄산이 깨지지 않게 바스푼으로 가볍게 저어주세요.','310','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('41','칵테일 셰이커에 진, 살구 브랜디, 칼바도스를 동량으로 넣어주세요.','311','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('41','얼음을 가득 채운 뒤 재료가 차가워지도록 세게 흔들어주세요.','312','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('41','식혀둔 칵테일 글라스에 얼음을 거르고 액체만 따라주세요.','313','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('42','올드 패션드 글라스에 얼음을 가득 채워주세요.','314','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('42','보드카 50mL와 깔루아 20mL를 잔에 따라주세요.','315','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('42','바스푼을 이용해 10~15초 정도 부드럽게 저어 완성해주세요.','316','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('43','믹싱 글라스에 진과 드라이 베르무트를 비율대로 넣어주세요.','317','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('43','얼음을 채우고 바스푼으로 부드럽게 저어 온도를 낮춰주세요.','318','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('43','차갑게 식힌 마티니 칵테일 글라스에 얼음을 거르고 맑은 술만 따라주세요.','319','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('44','믹싱 글라스에 라이 위스키, 스위트 베르무트, 비터스를 넣어주세요.','320','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('44','얼음을 넣고 온도가 낮아질 때까지 바스푼으로 잘 저어주세요.','321','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('44','차갑게 식힌 클래식 칵테일 잔에 얼음을 거르고 따라주세요.','322','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('45','스테인리스 주렙 컵 바닥에 민트, 설탕, 물을 넣고 향이 나도록 가볍게 짓이겨주세요.','323','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('45','잘게 부순 조각 얼음을 컵에 가득 채워주세요.','324','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('45','버번 위스키를 붓고 컵 표면에 서리가 맺힐 때까지 잘 저어주세요.','325','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('46','올드 패션드 잔에 각설탕을 넣고 비터스와 물을 떨어뜨려 주세요.','326','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('46','설탕이 완전히 녹을 때까지 머들러로 부드럽게 으깨주세요.','327','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('46','얼음과 위스키를 채우고 재료가 섞이도록 가볍게 저어주세요.','328','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('47','올드 패션드 글라스에 커다란 얼음을 가득 채워주세요.','329','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('47','스카치 위스키와 드람뷔를 잔에 정량대로 바로 부어주세요.','330','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('47','두 재료가 조화롭게 섞이도록 바스푼으로 가볍게 저어주세요.','331','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('48','믹싱 글라스에 코냑과 화이트 민트 리큐르를 넣어주세요.','332','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('48','얼음을 가득 넣고 바스푼으로 빠르고 부드럽게 저어주세요.','333','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('48','차갑게 식힌 마티니 칵테일 글라스에 얼음을 거르고 액체만 따라주세요.','334','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('49','긴 하이볼 잔에 그린 샤르트뢰즈, 파인애플 주스, 라임 주스, 팔레넘을 부어주세요.','335','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('49','조각 얼음을 가득 채운 뒤 스위즐 스틱이나 바스푼으로 힘차게 돌려 섞어주세요.','336','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('49','얼음을 한 번 더 채워 완성해주세요.','337','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('50','셰이커에 보드카, 깔루아, 시럽, 그리고 갓 뽑은 진한 에스프레소를 넣어주세요.','338','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('50','얼음을 채우고 에스프레소 크레마 거품이 잘 나도록 강하게 흔들어주세요.','339','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('50','식힌 마티니 잔에 거품과 함께 걸러 따라주세요.','340','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('51','셰이커에 다크 럼, 캄파리, 파인애플 주스, 라임 주스, 설탕 시럽을 넣어주세요.','341','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('51','얼음을 채우고 재료들이 완전히 혼합되도록 흔들어주세요.','342','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('51','얼음이 담긴 락 글라스에 시원하게 걸러서 따라주세요.','343','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('52','셰이커에 메즈칼, 옐로우 샤르트뢰즈, 아페롤, 라임 주스를 동량으로 넣어주세요.','344','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('52','얼음을 넣고 차가워질 때까지 타이밍에 맞춰 잘 흔들어주세요.','345','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('52','식혀둔 칵테일 잔에 얼음을 거르고 따라주세요.','346','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('53','셰이커에 위스키, 시럽, 레몬 주스, 달걀 흰자를 넣고 얼음과 함께 강하게 흔들어주세요.','347','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('53','얼음을 채운 락 글라스에 부드럽게 걸러서 따라주세요.','348','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('53','바스푼을 이용해 레드 와인을 표면에 조심스럽게 띄워 층을 만들어주세요.','349','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('54','셰이커에 버번 위스키, 아마로 노니노, 아페롤, 레몬 주스를 1:1:1:1 비율로 넣어주세요.','350','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('54','얼음을 채우고 밸런스가 잡히도록 잘 흔들어주세요.','351','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('54','차갑게 식힌 칵테일 글라스에 거른 뒤 완성해주세요.','352','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('55','셰이커 바닥에 신선한 생강 조각을 넣고 머들러로 단단하게 으깨주세요.','353','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('55','싱글몰트를 제외한 위스키, 레몬 주스, 꿀 시럽과 얼음을 넣고 흔든 뒤 얼음 잔에 거릅니다.','354','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('55','피트 향이 나는 싱글몰트 위스키를 음료 위에 가볍게 띄워주세요.','355','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('56','셰이커에 셰리 와인, 설탕, 오렌지와 레몬 조각을 넣어주세요.','356','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('56','얼음을 넣고 과즙이 잘 배어 나오도록 힘차게 흔들어주세요.','357','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('56','부순 얼음이 가득 담긴 주렙 컵에 내용물을 걸러서 부어주세요.','358','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('57','올드 패션드 글라스에 얼음을 가득 채워주세요.','359','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('57','캄파리와 스위트 레드 베르무트를 잔에 바로 따라주세요.','360','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('57','소다수를 살짝 더해 청량감을 준 뒤 바스푼으로 부드럽게 한 바퀴 저어주세요.','361','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('58','글라스 바닥에 생꿀, 물, 라임 주스를 넣고 바스푼으로 잘 섞어 벽면에 발라주세요.','362','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('58','잔에 가득 부순 얼음을 가득 채워주세요.','363','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('58','럼을 부은 뒤 바닥에서 위쪽 방향으로 강하게 저어 섞어주세요.','364','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('59','샴페인을 제외한 진, 레몬 주스, 설탕 시럽을 셰이커에 넣어주세요.','365','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('59','얼음과 함께 잘 흔든 후 길쭉한 샴페인 플루트 잔에 걸러서 따라주세요.','366','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('59','마지막으로 차가운 샴페인을 잔에 채우고 가볍게 저어주세요.','367','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('60','소다수를 제외한 진, 레몬 주스, 시럽을 셰이커에 넣어주세요.','368','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('60','얼음과 함께 잘 흔든 후 가늘고 긴 텀블러 글라스에 따라주세요.','369','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('60','마지막에 소다수를 살짝 부어 완성해주세요. (얼음은 넣지 않습니다)','370','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('61','얼음이 가득 찬 하이볼 글라스에 진, 레몬 주스, 심플 시럽을 부어주세요.','371','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('61','남은 공간에 소다수를 가득 채워주세요.','372','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('61','탄산이 깨지지 않도록 바스푼으로 아래위로 가볍게 저어주세요.','373','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('62','와인 글라스나 샴페인 잔 바닥에 크렘 드 카시스를 먼저 부어주세요.','374','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('62','그 위에 차갑게 칠링된 드라이 화이트 와인을 채운다.','375','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('62','카시스가 은은하게 섞이도록 가볍게 한 번 저어준다.','376','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('63','하이볼 잔에 민트 잎, 설탕, 라임 주스를 넣고 향이 올라오도록 가볍게 머들링 해주세요.','377','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('63','소다수를 살짝 붓고 잔에 부순 얼음을 가득 채운 뒤 럼을 넣고 남은 공간을 소다수로 채워 가볍게 저어주세요.','378','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('64','진저 비어를 제외한 코냑, 진, 라임 주스, 비터스를 셰이커에 넣어주세요.','379','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('64','얼음과 함께 잘 흔든 후, 거르지 않고 얼음째 콜린스 잔에 그대로 부어주세요.','380','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('64','남은 공간을 알싸한 진저 비어로 가득 채운 뒤 저어주세요.','381','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('65','믹싱 글라스에 보드카, 토마토 주스, 레몬 주스, 우스터소스와 향신료를 넣어주세요.','382','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('65','얼음을 채우고 바스푼으로 부드럽게 저어 칠링해주세요.','383','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('65','얼음이 가득 담긴 하이볼 잔이나 락 글라스에 거 걸러 따라주세요.','384','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('66','칵테일 셰이커에 시트론 보드카, 코인트루, 라임 주스, 크랜베리 주스를 넣어주세요.','385','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('66','얼음을 가득 채우고 화사한 핑크빛이 돌 때까지 잘 흔들어주세요.','386','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('66','차가운 마티니 잔에 얼음을 거르고 따라주세요.','387','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('67','셰이커에 메즈칼, 화이트 럼, 팔레넘, 마라스키노, 라임 주스, 시럽, 달걀 흰자를 넣어주세요.','388','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('67','얼음과 세차게 흔든 후 차가운 칵테일 잔에 걸러서 따라주세요.','389','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('67','취향에 따라 전통 점토 컵에 온더락으로 즐기셔도 좋습니다.','390','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('68','셰이커에 두 종류의 럼, 오렌지 큐라소, 아몬드 시럽, 라임 주스, 심플 시럽을 넣어주세요.','391','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('68','얼음을 넣고 시원하게 흔들어주세요.','392','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('68','얼음이 가득 담긴 더블 락 글라스나 하이볼 잔에 그대로 부어 완성해주세요.','393','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('69','하이볼 글라스에 얼음을 가득 채워주세요.','394','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('69','보드카, 피치 스냅스, 오렌지 주스, 크랜베리 주스를 정량대로 차례차례 부어주세요.','395','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('69','빨대로 가볍게 저어 이쁜 색감이 섞이도록 서빙해주세요.','396','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('70','셰이커에 진, 체리 브랜디, 코인트루, 베네딕틴, 파인애플/라임 주스, 그레나딘, 비터스를 넣습니다.','397','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('70','얼음을 채우고 재료가 완전히 섞이도록 강하게 흔들어주세요.','398','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('70','길쭉한 Hurricane 잔에 얼음을 걸러서 따라주세요.','399','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('71','얼음이 채워진 하이볼 글라스에 데킬라와 신선한 오렌지 주스를 바로 부어주세요.','400','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('71','그레나딘 시럽을 잔 벽면을 따라 조심스럽게 흘려 넣어주세요.','401','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('71','시럽이 바닥에 가라앉아 이쁜 노을빛 층이 생기도록 절대 젓지 말고 서빙해주세요.','402','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('72','전기 블렌더에 럼, 팔레넘, 올스파이스, 라임/오렌지 주스, 꿀 시럽, 비터스를 넣어주세요.','403','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('72','부순 얼음을 넣고 몇 초간 짧게 갈아주세요(플래시 블렌딩).','404','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('72','굽이 있는 잔에 붓고 부순 얼음을 더 채워 장식해주세요.','405','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('73','칵테일 셰이커에 화이트 럼, 코냑, 트리플 섹, 레몬 주스를 1:1:1_','406','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('73','7 비율로 넣어주세요.','407','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('73','얼음을 넣고 재료가 차가워지도록 리드미컬하게 흔들어주세요.','408','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('73','차갑게 식힌 클래식 칵테일 잔에 걸러서 따라주세요.','409','4');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('74','셰이커에 진, 라즈베리 시럽, 레몬 주스, 달걀 흰자 몇 방울을 넣어주세요.','410','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('74','얼음을 채우고 흰자 거품이 쫀쫀하게 올라올 때까지 세차게 흔들어주세요.','411','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('74','차갑게 식힌 마티니 글라스에 부드럽게 걸러서 따라주세요.','412','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('75','더블 올드 패션드 글라스에 얼음을 가득 채워주세요.','413','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('75','페르넷 브랑카 리큐르를 정량 부어주세요.','414','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('75','남은 공간을 청량한 콜라로 가득 채운 뒤 바스푼으로 가볍게 저어 완성해주세요.','415','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('76','올드 패션드 글라스에 단단한 각얼음을 채워주세요.','416','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('76','코냑과 아마레토 리큐르를 잔에 바로 부어주세요.','417','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('76','두 리큐르가 오묘하게 섞이도록 바스푼을 이용해 가볍게 서너 바퀴 저어주세요.','418','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('77','칵테일 셰이커에 보드카, 라즈베리 리큐르, 파인애플 주스를 넣어주세요.','419','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('77','얼음을 가득 넣고 거품이 부드럽게 날 때까지 잘 흔들어주세요.','420','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('77','차갑게 식힌 칵테일 글라스에 부드럽게 걸러서 따라주세요.','421','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('78','셰이커에 진, 살구 브랜디, 신선한 오렌지 주스를 비율대로 넣어주세요.','422','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('78','얼음을 채우고 오렌지 향이 잘 배어나도록 흔들어주세요.','423','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('78','차갑게 식힌 칵테일 글라스에 얼음을 걸러내고 맑게 따라주세요.','424','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('79','차가운 올드 패션드 잔 안쪽을 압생트로 코팅하듯 헹군(린스) 후 얼음을 담아두세요.','425','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('79','믹싱 글라스에 코냑, 각설탕, 비터스를 넣고 얼음과 저어줍니다.','426','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('79','준비한 잔의 얼음과 남은 압생트를 버린 뒤 믹싱 글라스의 술을 걸러 부어주세요.','427','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('80','셰이커에 진, 보드카, 릴레 블랑을 6:2:1 비율로 채워주세요.','428','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('80','얼음을 가득 채우고 영화 속 대사처럼 \'젓지 말고 흔들어서\' 강하게 믹싱해주세요.','429','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('80','차갑게 식힌 깊은 칵테일 잔에 얼음을 거르고 따라주세요.','430','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('81','밀크셰이크 믹서(또는 블렌더)에 두 종류의 럼, 패션후르츠 시럽, 라임 주스, 꿀 시럽을 넣습니다.','431','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('81','부순 얼음을 넣고 몇 초간 빠르게 돌려주세요.','432','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('81','굽이 있는 코포 글라스에 붓고 위에 부순 얼음을 소복하게 더 채워주세요.','433','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('82','셰이커 바닥에 이탈리안 바질 잎을 넣고 진과 함께 으깨어 향을 내주세요.','434','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('82','레몬 주스와 설탕 시럽, 얼음을 마저 채워주세요.','435','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('82','바질 조각이 걸러지도록 힘차게 흔든 뒤 차가운 잔에 이중 거름망으로 걸러 따라주세요.','436','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('83','셰이커에 화이트 럼, 파인애플 주스, 마라스키노, 그레나딘 시럽을 넣어주세요.','437','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('83','얼음을 가득 넣고 파인애플 거품이 부드럽게 나도록 잘 흔들어주세요.','438','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('83','차갑게 식힌 엘레강트 칵테일 잔에 걸러서 따라주세요.','439','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('84','셰이커에 드라이 진, 오렌지 주스, 압생트, 그레나딘 시럽을 정량대로 넣어주세요.','440','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('84','얼음을 채우고 재료들이 완전히 혼합되도록 흔들어주세요.','441','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('84','차갑게 식힌 칵테일 글라스에 얼음을 거르고 깔끔하게 따라주세요.','442','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('85','텀블러 잔이나 전용 토기 잔에 자메이카 럼, 라임 주스, 사탕수수 주스를 바로 부어주세요.','443','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('85','바스푼으로 재료를 가볍게 섞어주세요.','444','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('85','취향에 따라 물이나 얼음을 더해 농도를 조절해 즐겨주세요.','445','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('86','셰이커에 브랜디, 타우니 포트 와인, 그리고 신선한 달걀 노른자를 넣어주세요.','446','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('86','노른자가 비리지 않고 크리미하게 풀리도록 얼음과 함께 아주 강하게 흔들어주세요.','447','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('86','차가운 칵테일 잔에 걸러서 따라주세요.','448','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('87','셰이커에 바닐라 보드카, 엘더플라워 코디얼, 라임 주스, 꿀 시럽, 홍고추 슬라이스를 넣어주세요.','449','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('87','얼음과 흔들어 고추의 매콤함이 배어 나오게 합니다.','450','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('87','고추씨가 들어가지 않게 차가운 잔에 이중으로 걸러 따라주세요.','451','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('88','셰이커에 버번 위스키, 레몬 주스, 설탕 시럽, 달걀 흰자 몇 방울을 넣어주세요.','452','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('88','얼음을 가득 채우고 흰자 폼이 부드럽게 올라오도록 강하게 흔들어주세요.','453','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('88','콥러 글라스나 얼음을 채운 올드 패션드 잔에 걸러 따라주세요.','454','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('89','셰이커에 코냑, 브라운 카카오 리큐르, 신선한 크림을 1:1:1 동량으로 넣어주세요.','455','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('89','얼음을 채우고 크림의 묵직함이 부드러운 텍스처가 되도록 힘차게 흔들어주세요.','456','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('89','차가운 칵테일 잔에 걸러 따라 완성해주세요.','457','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('90','믹싱 글라스에 진, 드라이 베르무트, 캄파리를 넣어주세요.','458','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('90','얼음을 가득 채우고 재료가 투명함을 유지하도록 바스푼으로 부드럽게 저어주세요.','459','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('90','차갑게 식힌 클래식 칵테일 잔에 얼음을 거르고 맑게 따라주세요.','460','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('91','락 글라스 가장자리에 고급 천일염을 예쁘게 묻혀(리밍) 준비하고 얼음을 채워두세요.','461','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('91','셰이커에 데킬라, 그랑 마니에르, 라임 주스, 얼음을 넣고 10초간 강하게 흔들어주세요.','462','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('91','준비해 둔 잔에 부드럽게 걸러 따라주세요.','463','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('92','예열한 글라스에 따뜻한 블랙커피를 붓고 위스키와 설탕 1스푼을 넣어 녹을 때까지 저어주세요.','464','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('92','차가운 크림을 살짝 걸쭉하게 준비해주세요.','465','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('92','커피 표면에 바스푼을 대고 크림을 조심스럽게 흘려보내 섞이지 않고 층이 뜨도록 해주세요.','466','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('93','셰이커에 진, 그린 샤르트뢰즈, 마라스키노, 라임 주스를 동일한 비율로 넣어주세요.','467','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('93','얼음을 넣고 허브 향과 시트러스가 조화롭도록 잘 흔들어주세요.','468','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('93','차갑게 식힌 칵테일 잔에 거르고 따라주세요.','469','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('94','셰이커에 앙고스투라 비터스, 오르제 시럽, 레몬 주스, 라이 위스키를 넣어주세요.','470','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('94','비터스의 강렬한 향이 시럽과 밸런스를 이루도록 얼음과 잘 흔들어주세요.','471','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('94','차가운 칵테일 잔에 걸러서 따라 완성해주세요.','472','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('95','믹싱 글라스에 올드 톰 진, 드라이 베르무트, 마라스키노, 압생트, 오렌지 비터스를 넣습니다.','473','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('95','얼음을 채우고 맛이 깔끔하게 정돈되도록 바스푼으로 잘 저어주세요.','474','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('95','차가운 마티니 잔에 얼음을 거르고 액체만 따라주세요.','475','3');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('96','전기 블렌더에 세 종류의 럼, 라임 주스, 팔레넘, Donn’s Mix, 그레나딘, 비터스, 페르노를 넣습니다.','476','1');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('96','조각 얼음 170g을 넣고 몇 초간 빠르게 갈아주세요(펄스 블렌딩).','477','2');
INSERT INTO `cocktail_recipe_steps` (`cocktail_id`,`description`,`id`,`step_order`) VALUES ('96','길쭉한 텀블러 잔에 그대로 부어 시원하게 완성해주세요.','478','3');

-- mood_questions: 35 reference rows
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('친구가 \'오늘 한잔?\'이라고 하면?','1',1,'RANDOM','1','고민 없이 머릿속에 바로 떠오르는 나의 첫 반응을 골라주세요','친구가 \'오늘 한잔?\'이라고 하면?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('새로운 사람을 만나면?','2',1,'RANDOM','2','새로운 관계 속에서 자연스럽게 나타나는 나의 행동을 선택해 주세요','새로운 사람을 만나면?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('여행에서 가장 기대되는 것은?','3',1,'RANDOM','3','낯선 공간에서 나를 가장 설레게 만드는 요소를 골라주세요','여행에서 가장 기대되는 것은?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('스트레스를 받으면 가장 먼저 하는 행동은?','4',1,'RANDOM','4','지친 하루 끝에 나를 빠르게 해소해 주는 방식을 선택해 주세요','스트레스를 받으면 가장 먼저 하는 행동은?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('새로운 취미를 시작한다면?','5',1,'RANDOM','5','나의 호기심과 끌림이 가장 크게 향하는 분야를 골라주세요','새로운 취미를 시작한다면?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('여행 중 길을 잃었다면?','6',1,'RANDOM','6','예상치 못한 상황을 맞이했을 때 나만의 대처 스타일을 선택해 주세요','여행 중 길을 잃었다면?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('친구가 새로운 메뉴를 추천하면?','7',1,'RANDOM','7','새로운 제안을 접했을 때 내가 보이는 반응을 골라주세요','친구가 새로운 메뉴를 추천하면?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('친구와 카페에 갔는데 사람이 많다면?','8',1,'RANDOM','8','복잡한 상황에서 자연스럽게 취하는 행동을 선택해 주세요','친구와 카페에 갔는데 사람이 많다면?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('친구가 갑자기 여행을 가자고 한다면?','9',1,'RANDOM','9','충동적인 제안을 받았을 때 나의 순간적인 태도를 골라주세요','친구가 갑자기 여행을 가자고 한다면?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('가장 마음이 편해지는 순간은?','10',1,'RANDOM','10','내 마음이 가장 온전하고 유연해지는 때를 선택해 주세요','가장 마음이 편해지는 순간은?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('피곤한 날 가장 필요한 것은?','11',1,'RANDOM','11','에너지 충전이 필요할 때 본능적으로 당기는 걸 골라주세요','피곤한 날 가장 필요한 것은?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('여행지에서 가장 오래 머무를 곳은?','12',1,'RANDOM','12','내 취향껏 발길이 머무는 나만의 아지트를 선택해 주세요','여행지에서 가장 오래 머무를 곳은?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('친구들이 나를 데려갈 것 같은 곳은?','13',1,'RANDOM','13','주변 사람들이 떠올리는 나의 대표적인 이미지를 골라주세요','친구들이 나를 데려갈 것 같은 곳은?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('비 오는 날 가장 하고 싶은 것은?','14',1,'RANDOM','14','감성적인 날씨에 끌리는 나만의 행동을 선택해 주세요','비 오는 날 가장 하고 싶은 것은?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('처음 보는 음료를 고른다면?','15',1,'RANDOM','15','새로운 음료를 접할 때 나의 직관적인 선택 기준을 골라주세요','처음 보는 음료를 고른다면?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('오늘 가장 듣고 싶은 말은?','16',1,'RANDOM','16','지금 내 마음에 가장 깊은 위로가 되는 한 마디를 선택해 주세요','오늘 가장 듣고 싶은 말은?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('새로운 메뉴를 주문할 때 나는?','17',1,'RANDOM','17','메뉴판을 보며 결정을 내리는 나의 타입을 골라주세요','새로운 메뉴를 주문할 때 나는?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('친구들이 나를 가장 잘 표현한다고 생각하는 말은?','18',1,'RANDOM','18','타인이 바라보는 나의 가장 큰 매력 포인트를 선택해 주세요','친구들이 나를 가장 잘 표현한다고 생각하는 말은?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('주말이 갑자기 비게 된다면?','19',1,'RANDOM','19','나에게 찾아온 뜻밖의 여유 시간을 보낼 방법을 골라주세요','주말이 갑자기 비게 된다면?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('친구와 의견이 다를 때 나는?','20',1,'RANDOM','20','생각의 차이가 생겼을 때 나의 대화 스타일을 선택해 주세요','친구와 의견이 다를 때 나는?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('예상보다 일이 빨리 끝났다. 가장 먼저 드는 생각은?','21',1,'RANDOM','21','자유 시간이 생겼을 때 머릿속에 가장 먼저 뜨는 생각을 골라주세요','예상보다 일이 빨리 끝났다. 가장 먼저 드는 생각은?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('친구가 선물을 고르기 어렵다고 하면?','22',1,'RANDOM','22','누군가를 위해 조언할 때 나의 추천 기준을 선택해 주세요','친구가 선물을 고르기 어렵다고 하면?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('술 한 잔 하러 왔어요. 가장 먼저 눈이 가는 자리는?','23',1,'RANDOM','23','공간에 들어섰을 때 본능적으로 이끌리는 자리를 골라주세요','술 한 잔 하러 왔어요. 가장 먼저 눈이 가는 자리는?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('바텐더가 "오늘은 어떤 기분이세요?"라고 묻는다면?','24',1,'RANDOM','24','자연스럽게 대화를 시작하는 나의 분위기를 선택해 주세요','바텐더가 "오늘은 어떤 기분이세요?"라고 묻는다면?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('칵테일이 나오기까지 시간이 남았어요. 무엇을 할까요?','25',1,'RANDOM','25','기다림의 시간 동안 내가 편안하게 취하는 행동을 골라주세요','칵테일이 나오기까지 시간이 남았어요. 무엇을 할까요?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('새로운 칵테일 이름을 발견했어요. 어떻게 할까요?','26',1,'RANDOM','26','호기심을 자극하는 메뉴를 만났을 때의 반응을 선택해 주세요','새로운 칵테일 이름을 발견했어요. 어떻게 할까요?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('친구가 내 잔을 한입 달라고 한다면?','27',1,'RANDOM','27','내 음료를 나누는 순간에 나타나는 나만의 태도를 골라주세요','친구가 내 잔을 한입 달라고 한다면?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('칵테일을 다 마신 후 가장 먼저 드는 생각은?','28',1,'RANDOM','28','마지막 한 모금을 비우고 난 뒤 느껴지는 여운을 선택해 주세요','칵테일을 다 마신 후 가장 먼저 드는 생각은?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('친구가 갑자기 건배사를 하자고 한다면?','29',1,'RANDOM','29','갑작스럽게 흥이 오르는 순간에 나의 반응을 골라주세요','친구가 갑자기 건배사를 하자고 한다면?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('주문한 칵테일 색상이 예상과 완전히 다르게 나왔때 반응은?','30',1,'RANDOM','30','뜻밖의 상황을 맞이했을 때 나의 반응을 선택해 주세요','주문한 칵테일 색상이 예상과 완전히 다르게 나왔때 반응은?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('[각성 상태] 지금 내 머릿속은?','10001',1,'FIXED','1','깊게 생각하지 말고 지금 느껴지는 상태를 골라주세요','지금 내 머릿속은 어떤 상태인가요?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('[오늘의 기분] 지금 기분이 어때요?','10002',1,'FIXED','2','현재 내 감정에 가장 가까운 항목을 선택해주세요','오늘의 기분은 어떤가요?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('[피로도] 오늘 하루 어땠나요?','10003',1,'FIXED','3','오늘 하루를 가볍게 돌아보며 골라보세요','오늘 하루는 어떠셨나요?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('[맛의 방향] 지금 이 순간 끌리는 맛은?','10004',1,'FIXED','4','지금 막 떠오르는 직관적인 맛을 선택해주세요','지금 이 순간, 끌리는 맛은?');
INSERT INTO `mood_questions` (`content`,`id`,`is_active`,`question_type`,`sort_order`,`subtitle`,`title`) VALUES ('[강도] 오늘 술, 어느 정도로 마실 것 같아요?','10005',1,'FIXED','5','오늘 원하는 술자리의 텐션을 골라주세요','오늘 술, 어느정도로 마실 것 같아요?');

-- mood_question_options: 175 reference rows
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('달달한게 좋아','1','1','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('상큼한 거 좋지','2','1','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('분위기 좋은 바 가자','3','1','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('시원한 하이볼!','4','1','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('독한 걸로 가자','5','1','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('먼저 말을 건다','6','2','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('웃으며 분위기를 푼다','7','2','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('천천히 관찰한다','8','2','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('공통점을 찾는다','9','2','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('강한 인상을 남긴다','10','2','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('현지 디저트','11','3','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('새로운 음식','12','3','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('분위기 좋은 카페','13','3','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('액티비티','14','3','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('로컬 펍','15','3','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('단 걸 먹는다','16','4','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('밖으로 나간다','17','4','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('혼자 생각한다','18','4','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('찬물로 씻는다','19','4','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('술 한잔한다','20','4','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('베이킹','21','5','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('사진 찍기','22','5','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('독서','23','5','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('서핑','24','5','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('칵테일 만들기','25','5','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('목적지를 바꾼다','26','6','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('일단 걸어본다','27','6','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('지도를 본다','28','6','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('재밌다며 즐긴다','29','6','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('인터넷 검색한다','30','6','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('달달하면 먹는다','31','7','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('일단 먹어본다','32','7','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('후기를 찾아본다','33','7','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('괜찮아 보이면 먹는다','34','7','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('독하면 더 좋다','35','7','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('디저트를 먹으며 기다린다','36','8','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('다른 카페를 찾아본다','37','8','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('조용히 기다린다','38','8','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('테이크아웃해서 밖으로 간다','39','8','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('디저트는 패스하고 술집으로 간다','40','8','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('맛집이 많으면 간다','41','9','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('재밌을 것 같으면 간다','42','9','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('계획부터 세운다','43','9','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('당장 떠난다','44','9','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('주류가 유명하면 간다','45','9','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('달콤한 간식을 먹을 때','46','10','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('햇살 아래 산책할 때','47','10','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('혼자 조용히 있을 때','48','10','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('시원한 바람을 맞을 때','49','10','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('늦은 밤 한잔할 때','50','10','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('달콤한 간식','51','11','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('상큼한 과일','52','11','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('조용한 휴식','53','11','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('시원한 바람','54','11','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('강한 한잔','55','11','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('디저트 맛집','56','12','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('과일시장','57','12','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('로컬 카페','58','12','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('해변','59','12','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('펍','60','12','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('베이커리','61','13','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('브런치 카페','62','13','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('조용한 카페','63','13','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('계곡','64','13','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('칵테일 바','65','13','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('케이크 먹기','66','14','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('과일주스 마시기','67','14','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('책 읽기','68','14','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('드라이브','69','14','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('위스키 마시기','70','14','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('달달해 보이는 음료','71','15','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('색이 예쁜 음료','72','15','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('제일 익숙한 음료','73','15','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('가장 시원해보이는 음료','74','15','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('가장 강렬한 음료','75','15','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('푹 쉬어','76','16','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('기분 전환하자','77','16','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('천천히 생각해','78','16','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('밖으로 나가자','79','16','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('오늘 한 잔 하자','80','16','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('익숙해 보여도 맛있으면 된다','81','17','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('설명이 가장 흥미로운 걸 고른다','82','17','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('후기를 꼼꼼히 읽는다','83','17','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('처음 보는 조합을 고른다','84','17','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('가장 자극적으로 보이는 걸 고른다','85','17','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('편안하다','86','18','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('호기심이 많다','87','18','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('신중하다','88','18','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('에너지가 넘친다','89','18','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('카리스마 있다','90','18','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('맛있는 걸 먹으러 간다','91','19','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('새로운 장소를 찾아간다','92','19','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('집에서 책이나 영화를 본다','93','19','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('드라이브나 산책을 간다','94','19','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('술 한잔하며 하루를 보낸다','95','19','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('분위기를 좋게 만든다','96','20','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('새로운 방법을 제안한다','97','20','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('논리적으로 설명한다','98','20','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('일단 해보자고 한다','99','20','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('강하게 밀어붙인다','100','20','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('맛있는 거 먹자','101','21','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('어디 놀러 갈까?','102','21','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('여유롭게 쉬자','103','21','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('몸을 움직이고 싶다','104','21','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('오늘 한잔해야겠다','105','21','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('무난하게 좋아할 걸 추천한다','106','22','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('새로운 걸 추천한다','107','22','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('오래 쓸 수 있는 걸 추천한다','108','22','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('재밌는 걸 추천한다','109','22','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('인상적인 걸 추천한다','110','22','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('디저트가 보이는 창가','111','23','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('창문 앞 테이블','112','23','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('바텐더 앞','113','23','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('야외 테라스','114','23','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('조명이 어두운 바 자리','115','23','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('기분 좋아지는 한 잔이요','116','24','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('새로운 걸 마셔보고 싶어요','117','24','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('조용히 즐길게요','118','24','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('시원하게 마시고 싶어요','119','24','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('강렬한 걸로 주세요','120','24','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('사진부터 찍는다','121','25','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('메뉴판을 다시 읽는다','122','25','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('가게 분위기를 둘러본다','123','25','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('친구와 이야기한다','124','25','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('다음 잔을 고른다','125','25','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('이름이 귀여우면 주문','126','26','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('재료부터 본다','127','26','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('익숙한 메뉴를 고른다','128','26','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('일단 도전한다','129','26','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('도수가 높은지부터 본다','130','26','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('기꺼이 준다','131','27','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('대신 한입 바꿔 마신다','132','27','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('조금만 준다','133','27','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('재밌겠다며 바꿔 마신다','134','27','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('내 건 안 뺏긴다','135','27','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('디저트 먹고 싶다','136','28','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('다른 메뉴도 궁금하다','137','28','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('여운을 즐긴다','138','28','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('한 잔 더 가볍게 마실까?','139','28','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('다음 잔은 더 강하게','140','28','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('웃으면서 맞장구친다','141','29','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('재밌는 건배사를 한다','142','29','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('조용히 잔을 든다','143','29','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('크게 외친다','144','29','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('원샷부터 한다','145','29','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('오히려 예쁘다고 생각한다','146','30','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('한 모금 마셔보고 어떤 재료인지 맞혀본다','147','30','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('조용히 색을 감상하며 마신다','148','30','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('신기해하며 친구들한테 보여준다','149','30','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('색은 상관없고 도수가 맞게 나왔는지 확인한다','150','30','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('생각이 너무 많아서 멈추질 않아!','100001','10001','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('약간 복잡하긴 한데 그럭저럭','100002','10001','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('그냥저냥 별 생각 없어','100003','10001','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('좀 멍하고 의욕이 없는 상태','100004','10001','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('완전 무기력 아무 생각도 하기 싫어...','100005','10001','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('너무 신나!!','100006','10002','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('기분 좋은 편!','100007','10002','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('그냥저냥 평범한 하루','100008','10002','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('왠지 모르게 가라앉는 날...','100009','10002','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('말하기도 싫다','100010','10002','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('갓생 그 자체 완벽한 하루','100011','10003','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('그래도 해야할 일은 다 함!!','100012','10003','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('그냥 무난하게~','100013','10003','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('생각보다 조금 피곤 ㅠㅠ','100014','10003','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('지쳐 쓰러진 상태','100015','10003','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('달달하고 부드러운 맛','100016','10004','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('새콤달콤이 땡기는데?','100017','10004','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('딱히 없음 아무거나','100018','10004','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('깔끔하고 시원한거 마시고 싶어','100019','10004','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('쓰고~ 진하고~ 묵직하고~','100020','10004','5');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('오늘은 센 거 마셔도 될 듯','100021','10005','1');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('적당히 기분 좋을 정도~','100022','10005','2');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('딱 중간, 무난하게','100023','10005','3');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('살짝만, 취하면 안돼','100024','10005','4');
INSERT INTO `mood_question_options` (`content`,`id`,`mood_question_id`,`option_order`) VALUES ('거의 논알콜 수준','100025','10005','5');

-- mood_question_option_scores: 307 reference rows
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('1','SWEETNESS','1','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('2','SOURNESS','2','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('3','BITTERNESS','3','DELTA','0.40');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('4','ALCOHOL_INTENSITY','3','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('5','REFRESHING','4','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('6','ALCOHOL_INTENSITY','5','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('7','REFRESHING','5','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('8','REFRESHING','6','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('9','SWEETNESS','7','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('10','BITTERNESS','8','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('11','SOURNESS','9','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('12','ALCOHOL_INTENSITY','10','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('13','SWEETNESS','11','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('14','SOURNESS','12','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('15','BITTERNESS','13','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('16','REFRESHING','14','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('17','ALCOHOL_INTENSITY','15','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('18','SWEETNESS','16','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('19','BITTERNESS','16','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('20','SOURNESS','17','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('21','BITTERNESS','18','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('22','REFRESHING','19','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('23','ALCOHOL_INTENSITY','20','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('24','SWEETNESS','21','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('25','SOURNESS','22','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('26','BITTERNESS','23','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('27','REFRESHING','24','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('28','ALCOHOL_INTENSITY','25','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('29','SWEETNESS','26','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('30','SOURNESS','27','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('31','BITTERNESS','28','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('32','REFRESHING','29','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('33','ALCOHOL_INTENSITY','30','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('34','SWEETNESS','31','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('35','SOURNESS','32','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('36','BITTERNESS','33','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('37','REFRESHING','34','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('38','ALCOHOL_INTENSITY','35','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('39','SWEETNESS','36','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('40','SOURNESS','37','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('41','BITTERNESS','38','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('42','REFRESHING','39','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('43','ALCOHOL_INTENSITY','40','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('44','SWEETNESS','41','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('45','SOURNESS','42','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('46','BITTERNESS','43','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('47','REFRESHING','44','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('48','ALCOHOL_INTENSITY','45','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('49','SWEETNESS','46','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('50','SOURNESS','47','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('51','BITTERNESS','48','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('52','REFRESHING','49','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('53','ALCOHOL_INTENSITY','50','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('54','SWEETNESS','51','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('55','BITTERNESS','51','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('56','SOURNESS','52','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('57','ALCOHOL_INTENSITY','52','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('58','BITTERNESS','53','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('59','REFRESHING','53','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('60','REFRESHING','54','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('61','ALCOHOL_INTENSITY','54','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('62','ALCOHOL_INTENSITY','55','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('63','SWEETNESS','55','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('64','SWEETNESS','56','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('65','REFRESHING','56','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('66','SOURNESS','57','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('67','SWEETNESS','57','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('68','BITTERNESS','58','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('69','SOURNESS','58','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('70','REFRESHING','59','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('71','BITTERNESS','59','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('72','ALCOHOL_INTENSITY','60','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('73','REFRESHING','60','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('74','SWEETNESS','61','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('75','ALCOHOL_INTENSITY','61','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('76','SOURNESS','62','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('77','BITTERNESS','62','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('78','BITTERNESS','63','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('79','SWEETNESS','63','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('80','REFRESHING','64','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('81','ALCOHOL_INTENSITY','64','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('82','ALCOHOL_INTENSITY','65','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('83','REFRESHING','65','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('84','SWEETNESS','66','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('85','REFRESHING','66','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('86','SOURNESS','67','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('87','ALCOHOL_INTENSITY','67','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('88','BITTERNESS','68','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('89','SWEETNESS','68','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('90','REFRESHING','69','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('91','BITTERNESS','69','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('92','ALCOHOL_INTENSITY','70','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('93','SWEETNESS','70','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('94','SWEETNESS','71','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('95','REFRESHING','71','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('96','SOURNESS','72','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('97','BITTERNESS','72','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('98','BITTERNESS','73','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('99','SOURNESS','73','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('100','REFRESHING','74','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('101','SWEETNESS','74','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('102','ALCOHOL_INTENSITY','75','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('103','REFRESHING','75','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('104','SWEETNESS','76','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('105','ALCOHOL_INTENSITY','76','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('106','SOURNESS','77','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('107','BITTERNESS','77','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('108','BITTERNESS','78','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('109','REFRESHING','78','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110','REFRESHING','79','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('111','BITTERNESS','79','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('112','ALCOHOL_INTENSITY','80','DELTA','0.80');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('113','SWEETNESS','80','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('114','SWEETNESS','81','DELTA','0.40');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('115','BITTERNESS','81','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('116','SOURNESS','82','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('117','REFRESHING','82','DELTA','0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('118','BITTERNESS','83','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('119','SWEETNESS','83','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('120','REFRESHING','84','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('121','SOURNESS','84','DELTA','0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('122','ALCOHOL_INTENSITY','85','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('123','REFRESHING','85','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('124','SWEETNESS','86','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('125','ALCOHOL_INTENSITY','86','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('126','SOURNESS','87','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('127','REFRESHING','87','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('128','BITTERNESS','88','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('129','SWEETNESS','88','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('130','REFRESHING','89','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('131','SOURNESS','89','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('132','ALCOHOL_INTENSITY','90','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('133','REFRESHING','90','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('134','SWEETNESS','91','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('135','REFRESHING','91','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('136','SOURNESS','92','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('137','REFRESHING','92','DELTA','0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('138','BITTERNESS','93','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('139','SWEETNESS','93','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('140','REFRESHING','94','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('141','ALCOHOL_INTENSITY','94','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('142','ALCOHOL_INTENSITY','95','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('143','REFRESHING','95','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('144','SWEETNESS','96','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('145','BITTERNESS','96','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('146','SOURNESS','97','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('147','REFRESHING','97','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('148','BITTERNESS','98','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('149','SWEETNESS','98','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('150','REFRESHING','99','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('151','SOURNESS','99','DELTA','0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('152','ALCOHOL_INTENSITY','100','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('153','REFRESHING','100','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('154','SWEETNESS','101','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('155','ALCOHOL_INTENSITY','101','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('156','SOURNESS','102','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('157','REFRESHING','102','DELTA','0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('158','BITTERNESS','103','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('159','REFRESHING','103','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('160','REFRESHING','104','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('161','SOURNESS','104','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('162','ALCOHOL_INTENSITY','105','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('163','SWEETNESS','105','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('164','SWEETNESS','106','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('165','REFRESHING','106','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('166','SOURNESS','107','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('167','REFRESHING','107','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('168','BITTERNESS','108','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('169','SWEETNESS','108','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('170','REFRESHING','109','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('171','SOURNESS','109','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('172','ALCOHOL_INTENSITY','110','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('173','BITTERNESS','110','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('174','SWEETNESS','111','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('175','REFRESHING','111','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('176','SOURNESS','112','DELTA','0.40');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('177','REFRESHING','112','DELTA','0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('178','BITTERNESS','113','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('179','SWEETNESS','113','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('180','REFRESHING','114','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('181','ALCOHOL_INTENSITY','114','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('182','ALCOHOL_INTENSITY','115','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('183','REFRESHING','115','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('184','SWEETNESS','116','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('185','BITTERNESS','116','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('186','SOURNESS','117','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('187','REFRESHING','117','DELTA','0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('188','BITTERNESS','118','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('189','SWEETNESS','118','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('190','REFRESHING','119','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('191','ALCOHOL_INTENSITY','119','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('192','ALCOHOL_INTENSITY','120','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('193','REFRESHING','120','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('194','SWEETNESS','121','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('195','SOURNESS','121','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('196','SOURNESS','122','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('197','REFRESHING','122','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('198','BITTERNESS','123','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('199','SWEETNESS','123','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('200','REFRESHING','124','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('201','SOURNESS','124','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('202','ALCOHOL_INTENSITY','125','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('203','BITTERNESS','125','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('204','SWEETNESS','126','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('205','REFRESHING','126','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('206','SOURNESS','127','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('207','BITTERNESS','127','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('208','BITTERNESS','128','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('209','SWEETNESS','128','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('210','REFRESHING','129','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('211','SOURNESS','129','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('212','ALCOHOL_INTENSITY','130','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('213','REFRESHING','130','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('214','SWEETNESS','131','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('215','REFRESHING','131','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('216','SOURNESS','132','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('217','REFRESHING','132','DELTA','0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('218','BITTERNESS','133','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('219','SWEETNESS','133','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('220','REFRESHING','134','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('221','SOURNESS','134','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('222','ALCOHOL_INTENSITY','135','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('223','BITTERNESS','135','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('224','SWEETNESS','136','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('225','BITTERNESS','136','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('226','SOURNESS','137','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('227','REFRESHING','137','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('228','BITTERNESS','138','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('229','SWEETNESS','138','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('230','REFRESHING','139','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('231','ALCOHOL_INTENSITY','139','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('232','ALCOHOL_INTENSITY','140','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('233','REFRESHING','140','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('234','SWEETNESS','141','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('235','REFRESHING','141','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('236','SOURNESS','142','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('237','REFRESHING','142','DELTA','0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('238','BITTERNESS','143','DELTA','0.60');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('239','SWEETNESS','143','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('240','REFRESHING','144','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('241','SOURNESS','144','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('242','ALCOHOL_INTENSITY','145','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('243','REFRESHING','145','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('244','SWEETNESS','146','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('245','REFRESHING','146','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('246','SOURNESS','147','DELTA','0.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('247','BITTERNESS','148','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('248','SWEETNESS','148','DELTA','-0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('249','REFRESHING','149','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('250','SOURNESS','149','DELTA','0.20');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('251','ALCOHOL_INTENSITY','150','DELTA','0.70');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('252','REFRESHING','150','DELTA','-0.30');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110001','BITTERNESS','100001','ABSOLUTE','1.07');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110002','SOURNESS','100001','ABSOLUTE','3.07');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110003','BITTERNESS','100002','ABSOLUTE','1.43');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110004','SOURNESS','100002','ABSOLUTE','2.49');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110005','BITTERNESS','100003','ABSOLUTE','1.78');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110006','SOURNESS','100003','ABSOLUTE','1.91');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110007','BITTERNESS','100004','ABSOLUTE','2.14');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110008','SOURNESS','100004','ABSOLUTE','1.34');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110009','BITTERNESS','100005','ABSOLUTE','2.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110010','SOURNESS','100005','ABSOLUTE','0.76');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110011','ALCOHOL_INTENSITY','100006','ABSOLUTE','1.38');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110012','ALCOHOL_INTENSITY','100007','ABSOLUTE','2.02');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110013','ALCOHOL_INTENSITY','100008','ABSOLUTE','2.66');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110014','ALCOHOL_INTENSITY','100009','ABSOLUTE','3.31');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110015','ALCOHOL_INTENSITY','100010','ABSOLUTE','3.95');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110016','SWEETNESS','100011','ABSOLUTE','1.92');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110017','SOURNESS','100011','ABSOLUTE','3.07');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110018','SWEETNESS','100012','ABSOLUTE','1.99');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110019','SOURNESS','100012','ABSOLUTE','2.49');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110020','SWEETNESS','100013','ABSOLUTE','2.07');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110021','SOURNESS','100013','ABSOLUTE','1.91');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110022','SWEETNESS','100014','ABSOLUTE','2.14');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110023','SOURNESS','100014','ABSOLUTE','1.34');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110024','SWEETNESS','100015','ABSOLUTE','2.22');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110025','SOURNESS','100015','ABSOLUTE','0.76');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110026','SWEETNESS','100016','ABSOLUTE','2.22');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110027','SOURNESS','100016','ABSOLUTE','0.76');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110028','REFRESHING','100016','ABSOLUTE','0.72');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110029','BITTERNESS','100016','ABSOLUTE','1.07');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110030','SWEETNESS','100017','ABSOLUTE','2.14');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110031','SOURNESS','100017','ABSOLUTE','2.49');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110032','REFRESHING','100017','ABSOLUTE','0.72');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110033','BITTERNESS','100017','ABSOLUTE','1.07');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110034','SWEETNESS','100018','ABSOLUTE','2.07');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110035','SOURNESS','100018','ABSOLUTE','1.91');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110036','REFRESHING','100018','ABSOLUTE','1.35');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110037','BITTERNESS','100018','ABSOLUTE','1.78');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110038','SWEETNESS','100019','ABSOLUTE','1.99');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110039','SOURNESS','100019','ABSOLUTE','1.91');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110040','REFRESHING','100019','ABSOLUTE','2.62');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110041','BITTERNESS','100019','ABSOLUTE','1.43');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110042','SWEETNESS','100020','ABSOLUTE','1.92');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110043','SOURNESS','100020','ABSOLUTE','0.76');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110044','REFRESHING','100020','ABSOLUTE','0.08');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110045','BITTERNESS','100020','ABSOLUTE','2.50');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110046','ALCOHOL_INTENSITY','100021','ABSOLUTE','3.95');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110047','REFRESHING','100021','ABSOLUTE','0.08');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110048','ALCOHOL_INTENSITY','100022','ABSOLUTE','3.31');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110049','REFRESHING','100022','ABSOLUTE','0.72');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110050','ALCOHOL_INTENSITY','100023','ABSOLUTE','2.66');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110051','REFRESHING','100023','ABSOLUTE','1.35');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110052','ALCOHOL_INTENSITY','100024','ABSOLUTE','2.02');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110053','REFRESHING','100024','ABSOLUTE','1.99');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110054','ALCOHOL_INTENSITY','100025','ABSOLUTE','1.38');
INSERT INTO `mood_question_option_scores` (`id`,`metric_type`,`mood_question_option_id`,`score_type`,`score_value`) VALUES ('110055','REFRESHING','100025','ABSOLUTE','2.62');

-- terms: 3 reference rows
INSERT INTO `terms` (`content`,`created_at`,`id`,`is_active`,`is_required`,`term_type`,`title`,`version`) VALUES ('# Moodtail(무드테일) 이용약관

## 제1조 (목적)

이 약관은 Moodtail 팀(이하 "팀")이 제공하는 감정 기반 칵테일 추천 서비스 \'Moodtail\'(이하 "서비스")의 이용과 관련하여 팀과 이용자 간의 권리, 의무 및 책임사항, 기타 필요한 사항을 규정함을 목적으로 합니다.

## 제2조 (정의)

1. "서비스"란 회사가 제공하는 무드 테스트, 칵테일 추천, 히스토리 기록, 캐릭터 도감, 함께 고르기, 트렌드 집계 등 Moodtail의 서비스 일체를 의미합니다.
2. "회원"이란 이 약관에 동의하고 회사와 이용계약을 체결하여 아이디(이메일 또는 소셜 계정)를 부여받은 자를 말합니다.
3. "비회원(게스트)"이란 회원가입 없이 서비스 일부(무드 테스트 수행 및 당일 결과 확인)를 이용하는 자를 말합니다.
4. "닉네임"이란 회원 식별을 위해 회원가입 시 자동 부여되며 회원이 자유롭게 수정할 수 있는 명칭을 말합니다.
5. "테스트 결과"란 회원이 무드 테스트에 응답한 내용을 기반으로 산출된 감정·맛 지표 및 추천 칵테일 정보를 말합니다.
6. "초대 코드"란 \'같이 고르기\' 기능 이용을 위해 회원에게 1회 발급되는 영구 식별 코드를 말합니다.
7. "위치기반서비스"란 이용자의 모바일 기기 위치정보를 활용하여 현재 위치 및 날씨/기후에 맞는 칵테일을 추천하는 서비스를 말합니다.

## 제3조 (약관의 게시 및 개정)

1. 회사는 이 약관의 내용을 회원이 쉽게 알 수 있도록 서비스 초기 화면 또는 연결화면에 게시합니다.
2. 회사는 「전자상거래 등에서의 소비자보호에 관한 법률」, 「약관의 규제에 관한 법률」 등 관련 법령을 위배하지 않는 범위에서 이 약관을 개정할 수 있습니다.
3. 약관을 개정할 경우 적용일자 및 개정사유를 명시하여 적용일자 7일 전부터 서비스 내 공지합니다. 다만 회원에게 불리한 변경의 경우 30일 전에 공지합니다.
4. 회원이 개정약관의 적용에 동의하지 않는 경우 회원은 이용계약을 해지(회원 탈퇴)할 수 있습니다. 공지 후 명시된 기간 내 이의를 제기하지 않으면 개정약관에 동의한 것으로 봅니다.

## 제4조 (회원가입)

1. 이용자는 아래 두 가지 방법 중 하나로 회원가입을 신청할 수 있습니다.
    - 소셜 로그인: 카카오 또는 구글 계정을 통한 인증(이름, 이메일 정보 수집)
    - 일반 회원가입: 이메일 및 비밀번호를 직접 설정하는 방식
2. 회원가입 시 회사는 임의의 닉네임을 자동으로 부여하며, 이용자는 가입 절차 중 또는 가입 이후 마이페이지에서 언제든지 닉네임을 수정할 수 있습니다.
3. 회사는 다음 각 호에 해당하는 신청에 대하여 승낙을 하지 않거나 사후에 이용계약을 해지할 수 있습니다.
    - 실명이 아니거나 타인의 명의를 이용한 경우
    - 만 [14]세 미만 아동이 가입을 신청하는 경우
    - 이전에 이 약관에 의해 이용계약이 해지된 회원이 재가입을 신청하는 경우 (부정 이용 등 사유가 있는 경우)
4. 서비스는 술(칵테일)에 관한 정보를 제공하며, 실제 음주를 유도하거나 판매하지 않습니다.

## 제5조 (서비스의 내용)

회사가 제공하는 서비스는 다음과 같습니다.

1. 무드 테스트를 통한 감정·맛 지표 분석 및 칵테일 추천
2. 오늘의 칵테일, 커스텀 추천, 함께 고르기(초대 코드를 통한 궁합 매칭)
3. 히스토리(음주 기록, 날짜별 사진, 테스트 결과) 저장 및 캘린더 조회
4. 캐릭터 도감 수집 및 대표 캐릭터 설정
5. 월간 리포트, 트렌드 집계(전체 이용자 통계) 제공
6. 테스트 결과 및 리포트의 이미지 생성, SNS 공유
7. 그 밖에 회사가 추가 개발하거나 제휴를 통해 제공하는 일체의 서비스

## **제6조 (위치기반서비스에 관한 조항)**

1. 회사는 이용자의 위치정보를 이용하여 \'오늘의 칵테일 맞춤 추천\' 등의 위치기반서비스를 무료로 제공합니다.
2. 이용자는 모바일 기기의 설정 및 앱 내 권한 설정을 통해 위치정보 수집 허용 여부를 언제든지 변경할 수 있습니다.
3. 회사는 위치기반서비스 제공을 위해 수집한 위치정보를 칵테일 추천 결과 산출 목적으로만 일회성으로 이용하며, 별도의 서버에 저장하거나 보관하지 않고 즉시 파기합니다.
4. 회사는 이용자의 동의 없이 위치정보를 제3자에게 제공하지 않습니다.

## 제7조 (서비스 이용시간 및 변경·중단)

1. 서비스는 회사의 업무상 또는 기술상 특별한 지장이 없는 한 연중무휴, 1일 24시간 제공함을 원칙으로 합니다.
2. 회사는 시스템 점검, 서버 증설 및 교체, 그 밖의 기술상 사유로 서비스 제공을 일시 중단할 수 있으며, 이 경우 사전에 공지합니다. 다만 긴급한 경우 사후 공지할 수 있습니다.
3. 회사는 서비스의 전부 또는 일부를 회사의 정책 및 운영상 필요에 따라 수정, 중단, 변경할 수 있으며, 이 경우 관련 내용을 사전에 공지합니다.

## 제8조 (회원의 의무)

1. 회원은 다음 행위를 하여서는 안 됩니다.
    - 신청 또는 변경 시 허위내용을 등록하는 행위
    - 타인의 계정, 초대 코드 등 정보를 도용하는 행위
    - 회사가 게시한 정보를 변경하거나 서비스를 이용하여 얻은 정보를 회사의 사전 승낙 없이 복제·유통·상업적으로 이용하는 행위
    - 회사 및 제3자의 저작권 등 지적재산권을 침해하는 행위
    - 회사 및 제3자의 명예를 손상시키거나 업무를 방해하는 행위
    - 무드 테스트 결과, 초대 코드, 공유 이미지 등을 부정한 방법으로 조작하거나 반복적으로 남용하는 행위
    - 그 밖에 관련 법령에 위배되는 행위
2. 회원은 자신의 계정 정보를 선량한 관리자의 주의로 관리하여야 하며, 이를 제3자에게 이용하게 할 수 없습니다.

## 제9조 (계정 및 비밀번호 관리)

1. 회원은 본인의 계정과 비밀번호에 대한 관리 책임을 지며, 이를 제3자가 이용하도록 하여서는 안 됩니다.
2. 동일 계정에 대해 5회 연속 로그인에 실패할 경우, 회사는 계정 보호를 위해 일정 시간 로그인을 제한할 수 있습니다.
3. 회원은 계정이 도용되거나 제3자가 무단으로 사용하고 있음을 인지한 경우 즉시 회사에 통지하고 안내에 따라야 합니다.

## 제10조 (게시물 및 이용자 콘텐츠)

1. 회원이 히스토리 기능 등을 통해 업로드한 사진, 기록 등 콘텐츠에 대한 권리는 해당 회원에게 있습니다.
2. 회사는 서비스의 운영, 개선 및 홍보를 위하여 필요한 범위 내에서 회원의 테스트 결과 및 관련 정보를 가공하여 표시할 수 있으며, 이 경우 개인을 식별할 수 있는 형태로 제3자에게 제공하지 않습니다.
3. 트렌드 집계 기능에 활용되는 데이터는 개인을 식별할 수 없도록 통계 처리된 형태로만 사용됩니다.

## 제11조 (개인정보보호)

회사는 관련 법령이 정하는 바에 따라 회원의 개인정보를 보호하기 위해 노력하며, 개인정보의 수집, 이용, 제공 및 보호에 관한 사항은 별도로 정한 개인정보처리방침을 따릅니다.

## 제12조 (회사의 의무)

1. 회사는 관련 법령과 이 약관이 금지하거나 미풍양속에 반하는 행위를 하지 않으며, 지속적이고 안정적으로 서비스를 제공하기 위해 노력합니다.
2. 회사는 회원이 안전하게 서비스를 이용할 수 있도록 개인정보보호를 위한 보안시스템을 갖추어야 합니다.
3. 회사는 서비스 이용과 관련하여 회원으로부터 제기되는 의견이나 불만이 정당하다고 인정할 경우 이를 처리하여야 합니다.

## 제13조 (이용계약 해지 및 이용제한)

1. 회원은 마이페이지 내 [회원 탈퇴] 기능을 통해 언제든지 이용계약 해지를 신청할 수 있습니다. 탈퇴 시 재확인 절차(팝업)를 거쳐 최종 처리됩니다.
2. 회원 탈퇴 시 회사는 개인 식별 데이터(프로필, 히스토리, 테스트 결과, 캐릭터 도감 등)를 지체 없이 삭제합니다. 다만 트렌드 집계에 반영된 통계 데이터는 익명화되어 있어 삭제 대상에서 제외됩니다.
3. 관련 법령에 따라 회사가 일정 기간 정보를 보관해야 하는 의무가 있는 경우, 해당 정보는 그 목적 범위 내에서만 보관 후 파기합니다. (자세한 내용은 개인정보처리방침 참고)
4. 회사는 회원이 제7조의 의무를 위반하거나 서비스 운영을 방해한 경우, 사전 통지 후 서비스 이용을 제한하거나 이용계약을 해지할 수 있습니다.

## 제14조 (면책조항)

1. 회사가 제공하는 칵테일 추천, 맛 지표, 레시피 등 정보는 재미 및 참고 목적의 정보 제공이며, 의학적·건강상의 조언이 아닙니다.
2. 회사는 서비스를 통해 얻은 정보로 인한 회원의 음주 관련 의사결정 및 그 결과에 대해 책임을지지 않습니다. 과도한 음주는 건강에 해로우며, 회원은 관련 법령 및 본인의 건강 상태를 고려하여 책임감 있게 음주하여야 합니다.
3. 회사는 천재지변 또는 이에 준하는 불가항력으로 인하여 서비스를 제공할 수 없는 경우 책임이 면제됩니다.
4. 회사는 회원의 귀책사유로 인한 서비스 이용 장애에 대해 책임을 지지 않습니다.

## 제15조 (손해배상 및 분쟁해결)

1. 회사 또는 회원은 상대방에게 고의 또는 과실로 손해를 입힌 경우 이를 배상할 책임이 있습니다.
2. 이 약관과 관련하여 분쟁이 발생한 경우 회사와 회원은 이를 원만히 해결하기 위해 성실히 협의합니다.
3. 협의가 이루어지지 않을 경우 관련 법령에 따른 관할 법원에 소를 제기할 수 있습니다.

## 제16조 (준거법)

이 약관의 해석 및 회사와 회원 간의 분쟁에 대하여는 대한민국 법을 적용합니다.

## 부칙

이 약관은 2026년 8월 21일부터 시행합니다.','2026-07-24 08:34:52','1','1','1','SERVICE','Moodtail(무드테일) 이용약관','2.0');
INSERT INTO `terms` (`content`,`created_at`,`id`,`is_active`,`is_required`,`term_type`,`title`,`version`) VALUES ('# Moodtail(무드테일) 개인정보처리방침

> Moodtail 팀(이하 "팀")은 이용자의 개인정보를 중요시하며, 「개인정보 보호법」 등 관련 법령을 준수하고 있습니다.
> 

---

## 제1조 (수집하는 개인정보 항목 및 수집 방법)

팀은 서비스 제공을 위해 최소한의 개인정보를 수집하며, 수집하는 항목은 다음과 같습니다.

### 1. 수집 항목

- **회원가입 및 로그인:**
    - **소셜 로그인 (카카오, 구글):** 소셜 고유 식별자(ID), 이메일 주소, 프로필 이름
    - **일반 회원가입:** 이메일 주소, 비밀번호(암호화 저장), 닉네임
- **서비스 이용 과정에서 생성/수집되는 정보:**
    - **히스토리 및 다이어리:** 유저가 직접 업로드한 사진 파일, 마신 칵테일 기록, 날짜별 감정/테스트 결과 데이터
    - **공유 이미지:** 테스트 결과 및 리포트 공유 시 생성되는 이미지 파일
    - **1:1 문의하기:** 답변 수신용 이메일 주소, 문의 내용
    - **자동 수집 항목:** 접속 일시, 서비스 이용 기록, 기기 식별자(UUID/토큰)
    - **위치기반서비스 이용 시**: 모바일 기기의 위치 정보 (GPS 기반 위치 데이터)
- **모바일 기기 접근 권한 (선택 접근 권한):**
    - **카메라 권한:** 히스토리 사진 직접 촬영 및 등록
    - **사진/앨범(갤러리) 권한:** 히스토리 사진 선택 및 저장, 결과 이미지 갤러리 다운로드
    - **위치 권한**: 현재 위치 및 날씨 기반 오늘의 칵테일 맞춤 추천

### 2. 수집 방법

- 앱 내 회원가입, 소셜 연동, 프로필 수정, 무드 테스트 수행, 위치 권한 동의 및 위치 기반 서비스 이용, 히스토리 작성, 공유 카드 생성, 1:1 문의하기 접수 시 이용자가 직접 입력 및 등록하는 방식을 통해 수집합니다.

---

## 제2조 (개인정보의 수집 및 이용 목적)

팀은 수집한 개인정보를 다음의 목적을 위해 활용합니다.

1. **회원 관리 및 본인 식별:** 회원가입, 개인 식별, 부정 이용 방지, 가입 의사 확인, 회원 탈퇴 처리
2. **서비스 제공 및 기능 이행:**
    - 맞춤형 칵테일 추천 및 맛 지표 분석 결과 제공
    - 히스토리(달력 및 다이어리) 데이터 저장 및 조회
    - 테스트 결과 및 리포트의 공유용 이미지 생성 및 SNS 공유 지원
    - 캐릭터 도감 해금 및 마이페이지 관리
    - 1:1 문의 사항 접수 및 이메일 답변 피드백
3. **서비스 분석 및 통계 활용:**
    - 이용자의 서비스 이용 형태 분석을 통한 트렌드 통계 집계 및 서비스 개선 (단, 비식별 익명화 데이터 형태로만 활용)

---

## 제3조 (개인정보의 보유 및 이용 기간)

1. **회원 정보 및 서비스 데이터 (즉시 삭제):**
    - 이용자의 개인정보는 회원 탈퇴 신청 시 유예기간 없이 즉시 파기(Hard Delete)됩니다. 탈퇴 즉시 프로필, 히스토리, 개인 결과 데이터 등 일체의 식별 정보가 삭제됩니다.
2. **공유 이미지 데이터 (1개월 보관):**
    - 테스트 결과 및 리포트 공유를 위해 생성된 **공유용 이미지 파일은 생성일로부터 1개월(30일)간 보관 후 자동 파기**됩니다.
3. **기타 임시 데이터:**
    - 1:1 문의 내역: 문의 처리 완료 후 3개월간 보관 후 파기
4. **익명화 통계 데이터:**
    - 유저가 작성한 무드 테스트 수치 및 칵테일 선호도 데이터는 회원 탈퇴 시 개인 식별자(User ID, 이메일 등)가 완전히 제거된 **익명화 형태로 변환되어 서비스 통계/트렌드 집계 목적으로 보관**됩니다.
5. **위치 정보 (즉시 파기)**: 
- 위치 기반 칵테일 추천 서비스 제공 후 지체 없이 즉시 파기하며, 서버에 별도로 보관하거나 저장하지 않습니다.

---

## 제4조 (개인정보의 파기절차 및 파기방법)

1. **파기절차:** 이용자가 회원 탈퇴를 신청하거나 보유 기간(공유 이미지 1개월 등)이 경과한 정보는 지체 없이 파기 절차를 거칩니다.
2. **파기방법:**
    - **전자적 파일 형태:** DB 및 서버에 저장된 개인정보는 복구 및 재생할 수 없는 기술적 방법을 사용하여 영구 삭제합니다.
    - **S3 등 클라우드 저장소 이미지:** 회원 탈퇴 시 히스토리 사진은 즉시 삭제되며, 공유 이미지는 생성 후 1개월 경과 시 물리적 파일 및 연결 링크를 영구 파기합니다.

---

## 제5조 (개인정보의 제3자 제공 및 위탁)

팀은 이용자의 개인정보를 원칙적으로 외부에 제공하지 않습니다. 다만, 서비스 운영을 위해 필요한 경우에 한하여 아래와 같이 최소한의 시스템 업무를 위탁하여 운영합니다.

- **위탁 대상 및 업무 내용:**
    - **카카오 / 구글 (Kakao / Google):** 소셜 회원가입 및 로그인 인증
    - **AWS (Amazon Web Services) 또는 Cloudflare:** 히스토리 및 공유 이미지, 서비스 데이터의 안전한 cloud 저장소 보관

---

## 제6조 (이용자 및 법정대리인의 권리)

1. **연령 제한:** 본 서비스는 개인정보보호법상 법정대리인 동의 절차의 복잡성을 고려하여 **만 14세 미만 아동의 회원가입을 제한**합니다.
2. **권리 행사:** 이용자는 언제든지 앱 내 `[마이페이지]`를 통해 본인의 개인정보를 조회, 수정할 수 있으며, `[회원 탈퇴]` 기능을 통해 개인정보 수집 및 이용 동의를 철회(탈퇴)하여 개인정보의 즉시 삭제를 요청할 수 있습니다.

---

## 제7조 (앱 접근권한 안내 및 거부 방법)

1. 서비스는 앱 서비스 제공을 위해 아래의 디바이스 권한을 사용합니다.
    - **카메라 (선택):** 히스토리 사진 촬영
    - **사진 / 앨범 (선택):** 히스토리 사진 첨부 및 결과 카드 저장
    - **위치 (선택)**: 현재 위치 및 날씨 기반 칵테일 맞춤 추천
2. 선택 접근권한은 해당 기능을 사용할 때 허용이 필요하며, 비허용 시에도 해당 기능 외 서비스(무드 테스트, 추천 등)는 정상적으로 이용 가능합니다.
3. 접근권한 변경 방법: 스마트폰 설정 $\\rightarrow$ 애플리케이션 $\\rightarrow$ Moodtail $\\rightarrow$ 권한 메뉴에서 변경 가능합니다.

---

## 부칙

본 개인정보처리방침은 **2026년 8월 21일**부터 적용됩니다.','2026-07-24 08:34:52','2','1','1','PRIVACY','Moodtail(무드테일) 개인정보처리방침','2.0');
INSERT INTO `terms` (`content`,`created_at`,`id`,`is_active`,`is_required`,`term_type`,`title`,`version`) VALUES ('','2026-07-24 08:34:52','3','0','0','MARKETING','','1.0');

SET FOREIGN_KEY_CHECKS = 1;
