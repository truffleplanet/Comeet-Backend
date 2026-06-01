-- User Table
CREATE TABLE IF NOT EXISTS users
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    email             VARCHAR(255) NOT NULL UNIQUE,
    password          VARCHAR(255),
    nick_name         VARCHAR(255),
    profile_image_url TEXT,
    social_id         VARCHAR(255),
    role              VARCHAR(50)  NOT NULL DEFAULT 'GUEST',
    created_at        TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roasteries
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    logo_url    VARCHAR(500),
    website_url VARCHAR(500),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stores
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    roastery_id    BIGINT         NOT NULL,
    owner_id       BIGINT,
    name           VARCHAR(100)   NOT NULL,
    description    TEXT,
    address        VARCHAR(255)   NOT NULL,
    latitude       DECIMAL(10, 8) NOT NULL,
    longitude      DECIMAL(11, 8) NOT NULL,
    phone_number   VARCHAR(20),
    category       VARCHAR(50),
    thumbnail_url  VARCHAR(500),
    open_time      TIME,
    close_time     TIME,
    average_rating DECIMAL(3, 2)  NOT NULL DEFAULT 0,
    review_count   INT            NOT NULL DEFAULT 0,
    visit_count    INT            NOT NULL DEFAULT 0,
    is_closed      BOOLEAN        NOT NULL DEFAULT FALSE,
    deleted_at     TIMESTAMP,
    created_at     TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_average_rating CHECK (average_rating >= 0 AND average_rating <= 5.0),
    FOREIGN KEY (roastery_id) REFERENCES roasteries (id),
    FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS owner_applications
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT         NOT NULL,
    roastery_id       BIGINT         NOT NULL,
    store_name        VARCHAR(100)   NOT NULL,
    store_description TEXT,
    store_address     VARCHAR(255)   NOT NULL,
    latitude          DECIMAL(10, 8) NOT NULL,
    longitude         DECIMAL(11, 8) NOT NULL,
    opening_hours     VARCHAR(20),
    category          VARCHAR(50),
    phone_number      VARCHAR(20),
    thumbnail_url     VARCHAR(500),
    status            VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    reject_reason     VARCHAR(500),
    reviewed_by       BIGINT,
    reviewed_at       TIMESTAMP,
    created_at        TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_owner_applications_user_status (user_id, status),
    INDEX idx_owner_applications_status_created (status, created_at),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (roastery_id) REFERENCES roasteries (id),
    FOREIGN KEY (reviewed_by) REFERENCES users (id)
);

-- Passport Table (월별 커피 여권)
CREATE TABLE IF NOT EXISTS passports
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id               BIGINT  NOT NULL,
    year                  INT     NOT NULL COMMENT '연도 (2024, 2025 등)',
    month                 TINYINT NOT NULL COMMENT '월 (1-12)',
    cover_image_url       VARCHAR(500) COMMENT 'AI 생성 여권 커버 이미지 URL',
    total_coffee_count    INT     NOT NULL DEFAULT 0 COMMENT '해당 월 총 커피 마신 횟수',
    total_store_count     INT     NOT NULL DEFAULT 0 COMMENT '방문 카페 수',
    total_bean_count      INT     NOT NULL DEFAULT 0 COMMENT '마신 원두 종류 수',
    top_origin            VARCHAR(100) COMMENT '가장 많이 마신 원산지',
    top_roastery          VARCHAR(100) COMMENT '가장 많이 방문한 로스터리',
    origin_sequence       VARCHAR(500) COMMENT '원산지 순서 (쉼표 구분: Ethiopia,Colombia,Brazil)',
    total_origin_distance DECIMAL(10, 2) COMMENT '원산지 순서 기반 총 이동 거리 (km)',
    created_at            TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_passport_user_year_month (user_id, year, month),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS visits
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT         NOT NULL,
    menu_id     BIGINT         NOT NULL,
    latitude    DECIMAL(10, 8) NOT NULL,
    longitude   DECIMAL(11, 8) NOT NULL,
    is_verified BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS passport_visits
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    passport_id BIGINT NOT NULL,
    visit_id    BIGINT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_passport_visit (passport_id, visit_id),
    FOREIGN KEY (passport_id) REFERENCES passports (id) ON DELETE CASCADE,
    FOREIGN KEY (visit_id) REFERENCES visits (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reviews
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    visit_id   BIGINT        NOT NULL UNIQUE,
    user_id    BIGINT        NOT NULL,
    store_id   BIGINT        NOT NULL,
    menu_id    BIGINT        NOT NULL,
    content    TEXT,
    is_public  BOOLEAN       NOT NULL,
    image_url  TEXT,
    rating     DECIMAL(2, 1) NULL COMMENT '평점 (0.5 ~ 5.0, 0.5 단위)',
    deleted_at TIMESTAMP,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_rating CHECK (rating IS NULL OR (rating >= 0.5 AND rating <= 5.0)),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (visit_id) REFERENCES visits (id),
    FOREIGN KEY (store_id) REFERENCES stores (id)
    -- FOREIGN KEY (menu_id) REFERENCES menus (id),
);

CREATE TABLE IF NOT EXISTS flavors
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL COMMENT '고유 코드 (FRUITY, BERRY 등)',
    parent_id   BIGINT COMMENT '상위 카테고리 ID',
    level       TINYINT      NOT NULL COMMENT '1:대분류, 2:중분류, 3:소분류',
    path        VARCHAR(255) NOT NULL COMMENT '계층 경로 (fruity/berry/blackberry)',
    name        VARCHAR(100) NOT NULL COMMENT '이름',
    description VARCHAR(500) COMMENT '설명',
    color_hex   VARCHAR(7) COMMENT 'SCA Wheel 색상',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS tasting_notes
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id  BIGINT NOT NULL,
    flavor_id  BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (review_id) REFERENCES reviews (id),
    FOREIGN KEY (flavor_id) REFERENCES flavors (id)
);

CREATE TABLE IF NOT EXISTS cupping_notes
(
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id                BIGINT        NOT NULL UNIQUE COMMENT '리뷰당 1개의 커핑노트 (1:1)',
    roast_level              VARCHAR(50)   NULL COMMENT '로스팅 레벨',

    fragrance_score          DECIMAL(4, 2) NULL COMMENT '0.00-15.00',
    aroma_score              DECIMAL(4, 2) NULL COMMENT '0.00-15.00',
    flavor_score             DECIMAL(4, 2) NULL COMMENT '0.00-15.00',
    aftertaste_score         DECIMAL(4, 2) NULL COMMENT '0.00-15.00',
    acidity_score            DECIMAL(4, 2) NULL COMMENT '0.00-15.00',
    sweetness_score          DECIMAL(4, 2) NULL COMMENT '0.00-15.00',
    mouthfeel_score          DECIMAL(4, 2) NULL COMMENT '0.00-15.00',

    total_score              DECIMAL(5, 2) GENERATED ALWAYS AS (
        COALESCE(fragrance_score, 0) + COALESCE(aroma_score, 0) +
        COALESCE(flavor_score, 0) + COALESCE(aftertaste_score, 0) +
        COALESCE(acidity_score, 0) + COALESCE(sweetness_score, 0) +
        COALESCE(mouthfeel_score, 0)
        ) STORED COMMENT '총점: 0-105',

    fragrance_aroma_detail   TEXT          NULL,
    flavor_aftertaste_detail TEXT          NULL,
    acidity_notes            TEXT          NULL,
    sweetness_notes          TEXT          NULL,
    mouthfeel_notes          TEXT          NULL,
    overall_notes            TEXT          NULL,

    created_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS beans
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    roastery_id       BIGINT       NOT NULL,
    name              VARCHAR(100) NOT NULL,
    country           VARCHAR(50)  NOT NULL,
    farm              VARCHAR(100),
    variety           VARCHAR(100),
    processing_method VARCHAR(50),
    roasting_level    VARCHAR(50),
    deleted_at        TIMESTAMP,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (roastery_id) REFERENCES roasteries (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS menus
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id    BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    price       INT          NOT NULL,
    category    VARCHAR(50),
    image_url   VARCHAR(500),
    deleted_at  TIMESTAMP,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS menu_bean_mappings
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_id    BIGINT  NOT NULL,
    bean_id    BIGINT  NOT NULL,
    is_blended BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_menu_bean (menu_id, bean_id),
    FOREIGN KEY (menu_id) REFERENCES menus (id) ON DELETE CASCADE,
    FOREIGN KEY (bean_id) REFERENCES beans (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bean_flavor_notes
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    bean_id    BIGINT NOT NULL,
    flavor_id  BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_bean_flavor (bean_id, flavor_id),
    FOREIGN KEY (bean_id) REFERENCES beans (id) ON DELETE CASCADE,
    FOREIGN KEY (flavor_id) REFERENCES flavors (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bean_scores
(
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    bean_id                BIGINT   NOT NULL UNIQUE COMMENT 'beans 테이블 FK (1:1 관계)',
    acidity                TINYINT  NOT NULL DEFAULT 5 COMMENT '산미 (1-10): 1=Very Low, 5=Medium, 10=Very High',
    body                   TINYINT  NOT NULL DEFAULT 5 COMMENT '바디감 (1-10): 1=Very Light, 5=Medium, 10=Very Full',
    sweetness              TINYINT  NOT NULL DEFAULT 5 COMMENT '단맛 (1-10): 1=Very Low, 5=Medium, 10=Very High',
    bitterness             TINYINT  NOT NULL DEFAULT 5 COMMENT '쓴맛 (1-10): 1=Very Low, 5=Medium, 10=Very Strong',
    aroma                  TINYINT  NOT NULL DEFAULT 5 COMMENT '향 (1-10): 1=Weak, 5=Medium, 10=Intense',
    flavor                 TINYINT  NOT NULL DEFAULT 5 COMMENT '풍미 (1-10): 1=Simple, 5=Balanced, 10=Complex',
    aftertaste             TINYINT  NOT NULL DEFAULT 5 COMMENT '여운 (1-10): 1=Short, 5=Medium, 10=Long',

    total_score            TINYINT  NOT NULL DEFAULT 0 COMMENT '총점 (0-100): 외부 데이터 그대로 또는 cupping_notes 변환',
    roast_level            ENUM ('LIGHT', 'MEDIUM', 'HEAVY')
                                    NOT NULL DEFAULT 'MEDIUM' COMMENT '배전도 (라이트/미디엄/헤비)',
    created_at             TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_acidity CHECK (acidity BETWEEN 1 AND 10),
    CONSTRAINT chk_body CHECK (body BETWEEN 1 AND 10),
    CONSTRAINT chk_sweetness CHECK (sweetness BETWEEN 1 AND 10),
    CONSTRAINT chk_bitterness CHECK (bitterness BETWEEN 1 AND 10),
    CONSTRAINT chk_aroma CHECK (aroma BETWEEN 1 AND 10),
    CONSTRAINT chk_flavor CHECK (flavor BETWEEN 1 AND 10),
    CONSTRAINT chk_aftertaste CHECK (aftertaste BETWEEN 1 AND 10),
    CONSTRAINT chk_total_score CHECK (total_score BETWEEN 0 AND 100),

    FOREIGN KEY (bean_id) REFERENCES beans (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_preferences
(
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                  BIGINT  NOT NULL UNIQUE,
    pref_acidity             TINYINT NOT NULL DEFAULT 5 COMMENT '선호 산미 (1-10): 1=부드러움, 10=강렬함',
    pref_body                TINYINT NOT NULL DEFAULT 5 COMMENT '선호 바디감 (1-10): 1=가벼움, 10=묵직함',
    pref_sweetness           TINYINT NOT NULL DEFAULT 5 COMMENT '선호 단맛 (1-10): 1=드라이, 10=달콤함',
    pref_bitterness          TINYINT NOT NULL DEFAULT 5 COMMENT '선호 쓴맛 (1-10): 1=거의없음, 10=강함',

    preferred_roast_levels   JSON             DEFAULT NULL COMMENT '선호 배전도 목록 (JSON Array): LIGHT, MEDIUM, HEAVY',
    liked_tags               JSON             DEFAULT NULL COMMENT '선호 플레이버 태그 - Soft Scoring용',
    disliked_tags            JSON             DEFAULT NULL COMMENT '비선호 태그 - 하드 필터링으로 제외 (알러지 등)',
    created_at               TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_pref_acidity CHECK (pref_acidity BETWEEN 1 AND 10),
    CONSTRAINT chk_pref_body CHECK (pref_body BETWEEN 1 AND 10),
    CONSTRAINT chk_pref_sweetness CHECK (pref_sweetness BETWEEN 1 AND 10),
    CONSTRAINT chk_pref_bitterness CHECK (pref_bitterness BETWEEN 1 AND 10),

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookmark_folders
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL COMMENT '폴더 소유자',
    icon        VARCHAR(50)  NOT NULL DEFAULT 'bookmark-fill' COMMENT '폴더 아이콘 이름',
    name        VARCHAR(50)  NOT NULL COMMENT '폴더 이름',
    description VARCHAR(255) NULL COMMENT '폴더 설명',
    created_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_bookmark_folders_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookmark_items
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    folder_id BIGINT NOT NULL COMMENT '폴더 ID',
    store_id  BIGINT NOT NULL COMMENT '저장된 카페 ID',
    added_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '폴더에 추가한 시점',
    UNIQUE KEY uk_folder_store (folder_id, store_id) COMMENT '동일 폴더에 동일 카페 중복 저장 방지',
    INDEX idx_bookmark_items_folder_id (folder_id),
    FOREIGN KEY (folder_id) REFERENCES bookmark_folders (id) ON DELETE CASCADE,
    FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS country_coordinates
(
    country_name VARCHAR(50)    NOT NULL PRIMARY KEY COMMENT '국가명',
    latitude     DECIMAL(10, 6) NOT NULL COMMENT '위도',
    longitude    DECIMAL(11, 6) NOT NULL COMMENT '경도'
)
