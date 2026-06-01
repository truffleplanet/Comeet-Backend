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
