# 2025-12-15 #31
ALTER TABLE roasteries
    ADD COLUMN owner_id BIGINT NOT NULL;
ALTER TABLE roasteries
    ADD CONSTRAINT FOREIGN KEY (owner_id) REFERENCES users (id);

# 2025-12-17 #35
ALTER TABLE beans
    ADD COLUMN name VARCHAR(100) NOT NULL COMMENT '원두 이름' AFTER roastery_id;
# 38
ALTER TABLE roasteries
    DROP FOREIGN KEY roasteries_ibfk_1;

ALTER TABLE roasteries
    DROP COLUMN owner_id;

#2025-12-19 #47
CREATE UNIQUE INDEX uniq_passport_user_year_month ON passports (user_id, year, month);

-- 2. visits 테이블에 컬럼 추가
ALTER TABLE visits
    ADD COLUMN store_id BIGINT;
ALTER TABLE visits
    ADD COLUMN passport_id BIGINT;

-- 3. visits 테이블 외래키 추가
ALTER TABLE visits
    ADD FOREIGN KEY (store_id) REFERENCES stores (id);
ALTER TABLE visits
    ADD FOREIGN KEY (passport_id) REFERENCES passports (id);

-- 4. visits 테이블 인덱스 추가
CREATE INDEX idx_visit_passport ON visits (passport_id);
CREATE INDEX idx_visit_store ON visits (store_id);

ALTER TABLE passports
    ADD UNIQUE KEY uniq_passport_user_year_month (user_id, year, month);

-- 2025-12-23 #58: Review Rating 추가
ALTER TABLE reviews
    ADD COLUMN rating DECIMAL(2, 1) NULL COMMENT '평점 (0.5 ~ 5.0, 0.5 단위)' AFTER image_url;

ALTER TABLE reviews
    ADD CONSTRAINT chk_rating CHECK (rating IS NULL OR (rating >= 0.5 AND rating <= 5.0));

ALTER TABLE stores
    ADD CONSTRAINT chk_average_rating CHECK (average_rating >= 0 AND average_rating <= 5.0);

-- 2025-12-23 #50
ALTER TABLE menu_bean_mappings
    DROP is_blended;

-- 2026-06-01: 위치 기반 매장/추천 조회 BoundingBox 조건 최적화
DROP PROCEDURE IF EXISTS add_idx_stores_location_deleted;
DELIMITER //
CREATE PROCEDURE add_idx_stores_location_deleted()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'stores'
          AND index_name = 'idx_stores_location_deleted'
    ) THEN
        CREATE INDEX idx_stores_location_deleted
            ON stores (latitude, longitude, deleted_at);
    END IF;
END //
DELIMITER ;

CALL add_idx_stores_location_deleted();
DROP PROCEDURE IF EXISTS add_idx_stores_location_deleted;

-- 2026-06-02: 사용자별 방문 이력 최신순 조회 정렬 최적화
DROP PROCEDURE IF EXISTS add_idx_visits_user_created;
DELIMITER //
CREATE PROCEDURE add_idx_visits_user_created()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'visits'
          AND index_name = 'idx_visits_user_created'
    ) THEN
        CREATE INDEX idx_visits_user_created
            ON visits (user_id, created_at DESC);
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'visits'
          AND index_name = 'user_id'
    ) THEN
        DROP INDEX user_id ON visits;
    END IF;
END //
DELIMITER ;

CALL add_idx_visits_user_created();
DROP PROCEDURE IF EXISTS add_idx_visits_user_created;

-- 2026-06-02: 리뷰 수와 평점 통계 증분 갱신을 위한 누적 컬럼 추가
DROP PROCEDURE IF EXISTS add_store_review_stat_columns;
DELIMITER //
CREATE PROCEDURE add_store_review_stat_columns()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'stores'
          AND column_name = 'rating_count'
    ) THEN
        ALTER TABLE stores
            ADD COLUMN rating_count INT NOT NULL DEFAULT 0 AFTER review_count;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'stores'
          AND column_name = 'rating_sum'
    ) THEN
        ALTER TABLE stores
            ADD COLUMN rating_sum DECIMAL(10, 2) NOT NULL DEFAULT 0 AFTER rating_count;
    END IF;
END //
DELIMITER ;

CALL add_store_review_stat_columns();
DROP PROCEDURE IF EXISTS add_store_review_stat_columns;

UPDATE stores s
    LEFT JOIN (
        SELECT store_id,
               COUNT(*)                           AS review_count,
               COUNT(rating)                      AS rating_count,
               COALESCE(SUM(rating), 0)           AS rating_sum,
               COALESCE(ROUND(AVG(rating), 2), 0) AS average_rating
        FROM reviews
        WHERE deleted_at IS NULL
        GROUP BY store_id
    ) stats ON stats.store_id = s.id
SET s.review_count = COALESCE(stats.review_count, 0),
    s.rating_count = COALESCE(stats.rating_count, 0),
    s.rating_sum = COALESCE(stats.rating_sum, 0),
    s.average_rating = COALESCE(stats.average_rating, 0);

-- 2026-06-02: 가맹점주 역할명을 MANAGER에서 OWNER로 정리
UPDATE users
SET role = 'OWNER'
WHERE role = 'MANAGER';

-- 2026-06-02: 사업자 승인 정보와 관리자 검토 이력 추가
DROP PROCEDURE IF EXISTS add_owner_application_business_columns;
DELIMITER //
CREATE PROCEDURE add_owner_application_business_columns()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'owner_applications'
          AND column_name = 'business_registration_number'
    ) THEN
        ALTER TABLE owner_applications
            ADD COLUMN business_registration_number VARCHAR(20) NOT NULL DEFAULT '' AFTER longitude;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'owner_applications'
          AND column_name = 'representative_name'
    ) THEN
        ALTER TABLE owner_applications
            ADD COLUMN representative_name VARCHAR(100) NOT NULL DEFAULT '' AFTER business_registration_number;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'owner_applications'
          AND column_name = 'business_license_url'
    ) THEN
        ALTER TABLE owner_applications
            ADD COLUMN business_license_url VARCHAR(500) NOT NULL DEFAULT '' AFTER representative_name;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'owner_applications'
          AND column_name = 'pending_user_id'
    ) THEN
        ALTER TABLE owner_applications
            ADD COLUMN pending_user_id BIGINT GENERATED ALWAYS AS (
                CASE WHEN status = 'PENDING' THEN user_id ELSE NULL END
            ) STORED AFTER reviewed_at;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'owner_applications'
          AND index_name = 'uk_owner_applications_pending_user'
    ) THEN
        CREATE UNIQUE INDEX uk_owner_applications_pending_user
            ON owner_applications (pending_user_id);
    END IF;
END //
DELIMITER ;

CALL add_owner_application_business_columns();
DROP PROCEDURE IF EXISTS add_owner_application_business_columns;

CREATE TABLE IF NOT EXISTS owner_application_review_histories
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT       NOT NULL,
    reviewer_id    BIGINT       NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    comment        VARCHAR(500),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_owner_application_histories_application (application_id, created_at),
    FOREIGN KEY (application_id) REFERENCES owner_applications (id),
    FOREIGN KEY (reviewer_id) REFERENCES users (id)
);
