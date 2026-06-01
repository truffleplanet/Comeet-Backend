<div align="center">

# ☕ 커슐랭 (Cochelin)
[![개발 상태][status-shield]][status-url]

### ✨ 커피와 사람을 연결하는 특별한 만남 ✨
**메뉴 인증으로 시작하는 나만의 커피 여행**
</div>

---

## 📋 목차

- [📖 프로젝트 소개](#-프로젝트-소개)
- [🎯 프로젝트 목표](#-프로젝트-목표)
- [🛠️ 기술 스택](#️-기술-스택)
- [🚀 Quick Start](#-quick-start)
- [🌿 Git Branch 전략](#-git-branch-전략)
- [📝 Commit Message Convention](#-commit-message-convention)
- [📚 상세 문서](#-상세-문서)

---

# 📖 프로젝트 소개

> [!IMPORTANT]
> **커슐랭(Cochelin)**은 스페셜티 커피를 사랑하는 20-30대를 위한 커피 탐험 플랫폼입니다.
> 지도 기반으로 주변 카페를 발견하고, **메뉴를 인증**하며, **커피 테이스팅 노트**를 작성하는 새로운 경험을 제공합니다.

**핵심 차별점**:
- 🎯 **메뉴 인증 시스템**: GPS 기반 100m 반경 내 메뉴 인증
- ☕ **커피 원두 정보**: 생산 국가, 농장, 품종, 가공 방식 등 상세 정보
- 📝 **테이스팅 노트**: 초심자/심화자 템플릿으로 체계적인 커피 기록
- 🧪 **커핑 노트**: SCA 표준 7가지 항목 평가 (Fragrance, Aroma, Flavor 등)
- 📊 **커피 여권**: 국가별, 로스터리별 통계 및 방문 기록
- 🤖 **AI 추천**: 취향 기반 원두/메뉴 추천 (Vector Search + LLM)
- 📌 **북마크**: 폴더 기반 카페 저장 및 관리

---

# 🎯 프로젝트 목표

### 핵심 가치
- **접근성**: 커피 초보자부터 심화자까지 수준별 기록 템플릿 제공
- **탐험**: 지도 기반으로 새로운 카페를 발견하고 메뉴 인증
- **개인화**: AI 기반 취향 분석 및 맞춤 추천
- **기록**: 테이스팅 노트를 통한 체계적인 커피 기록 관리

### 타겟 사용자
- 스페셜티 커피에 관심 있는 20-30대
- 새로운 카페를 탐험하고 싶은 커피 애호가
- 체계적으로 커피를 기록하고 싶은 사용자
- 자신의 커피 취향을 찾아가고 싶은 초보자

---

# 🛠️ 기술 스택

## Backend (이 레포지토리)
| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7 |
| ORM | MyBatis 3.0.5 (XML Mapper) |
| Database | MySQL 8.0+ |
| Cache & Vector | Redis Stack (캐싱, 세션, 벡터 검색) |
| AI | Spring AI + OpenAI (임베딩, LLM 리랭킹), Gemini (여권 이미지 생성) |
| Security | Spring Security + JWT |
| API Docs | Swagger UI (SpringDoc) |

## Frontend (별도 레포지토리)
| 분류 | 기술 |
|------|------|
| Framework | Vue.js |
| Map | Naver Map API |

## Infrastructure
| 분류 | 기술 |
|------|------|
| Deployment | AWS / Naver Cloud Platform |
| Image Storage | AWS S3 |
| CI/CD | GitHub Actions |

---

# 🚀 Quick Start

## 요구 사항
- Java 21+
- MySQL 8.0+
- Redis Stack Server (벡터 검색 지원)

## 실행 방법

```bash
# 1. Redis Stack Server 실행
docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest

# 2. 프로젝트 클론
git clone https://github.com/S14-Comeet/Comeet-Backend.git
cd Comeet-Backend

# 3. MySQL에 comeet DB 생성
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS comeet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 4. 빌드 (테스트 제외)
./gradlew clean build -x test

# 5. 실행
./gradlew bootRun
```

## 데이터베이스 초기화

`local` 프로필은 실행 시 `schema/schema.sql`과 `local/seed.sql`을 자동 실행합니다.
로컬 seed에는 API 실험용 계정이 포함되어 있으며 공통 비밀번호는 `password1234`입니다.

| 역할 | 이메일 |
|------|------|
| USER | `user@example.com` |
| MANAGER | `manager@example.com` |
| ADMIN | `admin@example.com` |

원격/운영 데이터 초기화가 필요한 경우 SQL 파일은 아래 순서대로 실행합니다:

```bash
# 1. 스키마 생성
mysql -u [user] -p [database] < src/main/resources/sql/schema/schema.sql

# 2. 스키마 변경사항 적용 (필요시)
mysql -u [user] -p [database] < src/main/resources/sql/schema/change.sql

# 3. 기초 데이터 입력
mysql -u [user] -p [database] < src/main/resources/sql/data/flavor_prod.sql
mysql -u [user] -p [database] < src/main/resources/sql/data/country_coordinates.sql

# 4. 메인 데이터 임포트
mysql -u [user] -p [database] < src/main/resources/sql/data/data_import.sql

# 5. 데이터 정규화
mysql -u [user] -p [database] < src/main/resources/sql/data/normalize_bean_country_and_processing.sql
```

| 순서 | 파일 | 설명 |
|------|------|------|
| 1 | `schema/schema.sql` | 테이블 생성 |
| 2 | `schema/change.sql` | 스키마 변경사항 |
| 3 | `local/seed.sql` | 로컬 개발용 최소 사용자/로스터리 데이터 |
| 4 | `data/flavor_prod.sql` | 플레이버 휠 마스터 데이터 |
| 5 | `data/country_coordinates.sql` | 국가별 좌표 데이터 |
| 6 | `data/data_import.sql` | 카페, 메뉴, 원두 등 메인 데이터 |
| 7 | `data/normalize_bean_country_and_processing.sql` | 원두 국가/가공방식 정규화 |

## API 문서
서버 실행 후 Swagger UI에서 API 문서를 확인할 수 있습니다:
- **Local**: http://localhost:8080/

---

# 🌿 Git Branch 전략

## GitHub Flow
```
- master: 프로덕션 배포 브랜치
- feature/#issue: 기능 개발 브랜치 (예: feature/1, feature/2)
```

## 워크플로우
1. **기능 개발**: `feature/#issue` 브랜치에서 작업
2. **완료 후**: `master` 브랜치로 직접 Merge Request
3. **코드 리뷰**: MR 승인 후 머지
4. **배포**: `master` 브랜치 자동 배포

---

# 📝 Commit Message Convention

## 기본 형식 (Angular Commit Convention)
```
[gitmoji] type(#issue): subject

[optional body]

[optional footer]
```

## Type
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅, 세미콜론 누락 등 (코드 변경 없음)
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드 추가/수정
- `chore`: 빌드, 패키지 매니저 설정 등
- `perf`: 성능 개선
- `ci`: CI/CD 설정 변경
- `build`: 빌드 시스템 변경

## Scope (필수)
- `#이슈번호` 형식으로 작성
- 예: `#1`, `#42`, `#123`

## Subject (필수)
- 50자 이내로 간결하게 작성
- 마침표 없이 명령문으로 작성
- 한글 또는 영문 사용 가능

## Gitmoji (선택)
- 커밋 타입을 시각적으로 표현
- **사용 여부는 선택 사항**

### 자주 사용하는 Gitmoji
| Emoji | Code | Type |
|-------|------|------|
| ✨ | `:sparkles:` | feat (새 기능) |
| 🐛 | `:bug:` | fix (버그 수정) |
| 📝 | `:memo:` | docs (문서) |
| ♻️ | `:recycle:` | refactor (리팩토링) |
| ✅ | `:white_check_mark:` | test (테스트) |
| 🔧 | `:wrench:` | chore (설정) |
| ⚡ | `:zap:` | perf (성능) |
| 💄 | `:lipstick:` | style (스타일) |
| 🔥 | `:fire:` | 코드/파일 삭제 |
| 🚀 | `:rocket:` | 배포 |

---

## 📚 상세 문서

프로젝트의 상세한 설계 및 구현 가이드는 다음 문서를 참고하세요:

- **[Comeet_기능명세서.xlsx](./Comeet_기능명세서.xlsx)** - 상세 기능 명세

---

<div align="center">

### ✨ **커슐랭과 함께 나만의 커피 여행을 시작하세요!** ✨

*Made with ❤️ by Cochelin Team*

</div>

[status-url]: #
[status-shield]: https://img.shields.io/badge/status-in%20development-yellow?style=for-the-badge
